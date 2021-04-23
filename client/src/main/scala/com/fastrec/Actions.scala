package com.fastrec

import org.scalajs.dom
import diode.Action
import com.fastrec.domain.model.{Media, MediaRecorderStatus, MediaType}
import com.fastrec.domain.model.MediaRecorder
import com.fastrec.domain.model.MediaFilter
import com.fastrec.domain.model.MediaId
import scala.scalajs.js.typedarray.ArrayBuffer

case object NoOp extends Action

object RecorderAction {
  case object Start                                  extends Action
  case object StartCamera                            extends Action
  case class Started(recorder: MediaRecorder)        extends Action
  case class Stop(id: MediaId)                       extends Action
  case class Stopped(media: Media)                   extends Action
  case class UpdateState(state: MediaRecorderStatus) extends Action
  case class EditTitle(title: String)                extends Action
}

object VideoAction {
  case class Add(media: Media)                            extends Action
  case class SelectMedia(media: Option[Media])            extends Action
  case class LoadMedia(mediaList: List[Media])            extends Action
  case class DeleteMedia(media: Media)                    extends Action
  case class Convert(mediaType: MediaType)                extends Action
  case object ApplyEdit                                   extends Action
  case class AppliedEdit(media: Media)                    extends Action
  case object Reset                                       extends Action
  case class Concat(mediaId: MediaId, mediaId2: MediaId)  extends Action
  case class SetVideoSize(width: Int, height: Int)        extends Action
  case class SetFilter(filter: MediaFilter)               extends Action
  case object Download                                    extends Action
  case class Next(nextIndex: Int, media: Option[Media])   extends Action
  case class Prev(prevIndex: Int, media: Option[Media])   extends Action
  case class OpenFile(file: dom.File)                     extends Action
  case class OpenedFile(file: dom.File, buf: ArrayBuffer) extends Action
  case class EditTitle(media: Media, title: String)       extends Action
  case class EndEditTitle(media: Media)                   extends Action
}
