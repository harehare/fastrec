package com.fastrec

import scala.scalajs.js
import com.fastrec.domain.model.{Model, Media};
import diode._
import diode.react.ReactConnector
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import com.fastrec.facade.RecordRTC
import org.scalajs.dom.raw.URL
import com.fastrec.domain.model.{Processing, Ready, MediaType}
import com.fastrec.domain.model.MediaRecorder
import com.fastrec.domain.model.MediaRecorderStatus
import com.fastrec.domain.model.Recorder
import com.fastrec.domain.model.MediaFilter.{Brightness, Contrast, Saturation, Speed}

import eu.timepit.refined.auto._
import com.fastrec.domain.model.{MediaId, MediaUrl}
import com.fastrec.domain.model.Video
import scala.concurrent.Promise
import org.scalajs.dom.raw.FileReader
import scala.scalajs.js.typedarray.ArrayBuffer
import org.scalajs.dom.raw.Blob

object AppCircuit extends Circuit[Model] with ReactConnector[Model] with AppModule {

  def parseFile(file: dom.File) = {
    val result = Promise[ArrayBuffer]()
    val reader = new FileReader()

    if (file.`type`.indexOf("video") != -1) {
      reader.onload = (_: dom.UIEvent) => {
        result.success(reader.result.asInstanceOf[ArrayBuffer])
      }
      reader.readAsArrayBuffer(file)
    } else {
      result.failure(new Exception())
    }
    result.future
  }

  def openFile(file: dom.File) =
    Effect(
      parseFile(file)
        .map(VideoAction.OpenedFile(file, _))
    )

  def startRecording() =
    Effect(
      MediaRecorder
        .desktop()
        .map(RecorderAction.Started(_))
    )

  def startCameraRecording() =
    Effect(
      MediaRecorder
        .camera()
        .map(RecorderAction.Started(_))
    )

  def stopRecording(id: MediaId, recorder: MediaRecorder) =
    Effect(
      for {
        m <- recorder.stop()
        _ <- mediaService.save(m)
      } yield RecorderAction.Stopped(m.copy(id = id))
    )

  def updateRecordState(recorder: MediaRecorder) =
    Effect(
      recorder
        .state()
        .map(state => RecorderAction.UpdateState(state))
    )

  def convertMediaType(media: Media, dest: MediaType) =
    Effect(
      media.changeMediaType(dest).map(VideoAction.AppliedEdit(_))
    )

  def deleteMedia(media: Media) =
    Effect(
      for {
        _         <- mediaService.delete(media)
        mediaList <- mediaService.findAll()
      } yield VideoAction.LoadMedia(mediaList)
    )

  def saveMedia(media: Media) =
    Effect(
      mediaService.save(media).map(_ => NoOp)
    )

  def editApply(media: Media) =
    Effect(
      media
        .applyEdit()
        .map(VideoAction.AppliedEdit(_))
    )

  def concat(newId: MediaId, media1: Media, media2: Media) =
    Effect(
      for {
        m <- media1.concat(media2)
        newMedia = m.copy(id = newId)
        _ <- mediaService.save(newMedia)
      } yield VideoAction.Add(newMedia)
    )

  val videoHandler = new ActionHandler(zoomTo(_.video)) {
    override def handle = {
      case NoOp =>
        noChange

      case VideoAction.Add(media) => {
        updated(
          value.copy(
            editMediaId = Some(media.id),
            mediaItems = value.mediaItems + (media.id -> media),
            mediaList = media.id :: value.mediaList,
            mediaSelectedIndex = value.mediaList.length + 1
          )
        )
      }

      case VideoAction.ApplyEdit =>
        value.editMedia match {
          case Some(media) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media
                  .copy(data = Processing(0)))
              ),
              editApply(media)
            )
          }
          case None => noChange
        }

      case VideoAction.AppliedEdit(media) =>
        updated(
          value.copy(
            mediaItems = value.mediaItems + (media.id -> media
              .copy(
                brightness = Brightness(0.0),
                contrast = Contrast(0),
                saturation = Saturation(1.0)
              ))
          )
        )

      case VideoAction.Concat(id1, id2) => {
        val newId = Media.newId()
        (value.mediaItems.get(id1), value.mediaItems.get(id2)) match {
          case (Some(media1), Some(media2)) =>
            updated(
              value,
              concat(newId, media1, media2)
            )
          case _ => noChange
        }
      }

      case VideoAction.Convert(mediaType) =>
        value.editMediaId.flatMap(value.mediaItems.get(_)) match {
          case Some(media) =>
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media
                  .copy(data = Processing(0)))
              ),
              convertMediaType(media, mediaType)
            )
          case None => noChange
        }

      case VideoAction.Download =>
        value.editMedia match {
          case Some(media) =>
            media.data match {
              case Ready(blob) =>
                RecordRTC.invokeSaveAsDialog(
                  blob,
                  s"${media.name.getOrElse("rec")}${MediaType.extension(media.mediaType)}"
                )
                noChange
              case _ =>
                noChange
            }
            noChange
          case None => noChange
        }

      case VideoAction.SetVideoSize(width, height) =>
        value.editMedia match {
          case Some(media) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media.copy(
                  size = (width, height),
                  originalSize = media.originalSize.orElse(Some((width, height)))
                ))
              )
            )
          }
          case None => noChange
        }

      case VideoAction.SetFilter(filter) =>
        (value.editMedia, filter) match {
          case (Some(media), Brightness(b)) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media.copy(
                  brightness = Brightness(b)
                ))
              )
            )
          }
          case (Some(media), Contrast(c)) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media.copy(
                  contrast = Contrast(c)
                ))
              )
            )
          }
          case (Some(media), Saturation(s)) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media.copy(
                  saturation = Saturation(s)
                ))
              )
            )
          }
          case (Some(media), Speed(s)) => {
            updated(
              value.copy(
                mediaItems = value.mediaItems + (media.id -> media.copy(
                  speed = Speed(s)
                ))
              )
            )
          }
          case _ => noChange
        }

      case VideoAction.SelectMedia(Some(media)) =>
        updated(
          value.copy(
            editMediaId = Some(media.id),
            baseMedia = value.mediaItems.get(media.id),
            mediaItems = value.mediaItems + (media.id -> media)
          )
        )

      case VideoAction.SelectMedia(None) =>
        updated(
          value.copy(
            editMediaId = None
          )
        )

      case VideoAction.DeleteMedia(media) =>
        updated(value, deleteMedia(media))

      case VideoAction.LoadMedia(mediaList) =>
        updated(value.copy(mediaItems = mediaList.foldLeft(Map[MediaId, Media]()) {
          (mediaList, media) =>
            mediaList + (media.id -> media)
        }, mediaList = mediaList.map(_.id)))

      case VideoAction.Reset =>
        (for {
          id           <- value.editMediaId
          media        <- value.baseMedia
          currentMedia <- value.mediaItems.get(id)
          _ = URL.revokeObjectURL(currentMedia.data.toString())
        } yield
          updated(
            value.copy(
              mediaItems = value.mediaItems + (media.id -> currentMedia
                .copy(
                  data = media.data,
                  brightness = Brightness(0.0),
                  contrast = Contrast(0),
                  saturation = Saturation(1.0)
                ))
            )
          ))
          .getOrElse(noChange)

      case VideoAction.Prev(index, media) =>
        updated(
          value.copy(
            mediaSelectedIndex = index
          ),
          Effect.action(VideoAction.SelectMedia(media))
        )

      case VideoAction.Next(index, media) =>
        updated(
          value.copy(
            mediaSelectedIndex = index
          ),
          Effect.action(VideoAction.SelectMedia(media))
        )

      case VideoAction.OpenFile(file) =>
        updated(value, openFile(file))

      case VideoAction.OpenedFile(file, buf) => {
        val blob = new Blob(js.Array(buf))
        updated(
          value,
          Effect.action(
            VideoAction.Add(
              Media(MediaUrl(URL.createObjectURL(blob)), blob, MediaType.fromFileName(file.name))
                .copy(name = Some(file.name))
            )
          )
        )
      }

      case VideoAction.EditTitle(media, title) =>
        updated(
          value.copy(mediaItems = value.mediaItems + ((media.id -> media.copy(name = Some(title)))))
        )

      case VideoAction.EndEditTitle(media) =>
        updated(
          value,
          saveMedia(media)
        )
    }
  }

  val recorderHandler = new ActionHandler(zoomTo(_.recorder)) {
    override def handle = {
      case NoOp =>
        noChange

      case RecorderAction.Start =>
        updated(value, startRecording())

      case RecorderAction.StartCamera =>
        updated(value, startCameraRecording())

      case RecorderAction.Started(recorder) => {
        recorder.start()
        updated(
          value.copy(recorder = Some(recorder)),
          updateRecordState(recorder)
        )
      }

      case RecorderAction.UpdateState(state) =>
        updated(value.copy(status = state))

      case RecorderAction.Stop(id) => {
        value.recorder match {
          case Some(rec) =>
            updated(value, stopRecording(id, rec))
          case None => noChange
        }
      }

      case RecorderAction.Stopped(media) =>
        value.recorder match {
          case Some(rec) =>
            updated(
              value,
              Effect
                .action(VideoAction.SelectMedia(Some(media.copy(name = value.title)))) + Effect
                .action(VideoAction.Add(media)) + updateRecordState(
                rec
              )
            )
          case None => noChange
        }

      case RecorderAction.EditTitle(title) =>
        updated(value.copy(title = if (title == "") None else Some(title)))
    }
  }

  override protected def initialModel = {
    Model(
      Recorder(None, None, MediaRecorderStatus.Inactive, None),
      Video(None, None, Map[MediaId, Media](), Nil, 0)
    )
  }

  override protected val actionHandler = composeHandlers(
    recorderHandler,
    videoHandler
  )
}
