package com.fastrec.domain.model

import java.{util => ju}

import scala.scalajs.js
import org.scalajs.dom.raw.Blob
import com.fastrec.facade.FFmpeg
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.raw.BlobPropertyBag
import com.fastrec.facade.BlobUtil
import scala.scalajs.js.typedarray.Uint8Array
import com.fastrec.domain.model.MediaFilter.{Brightness, Contrast, Saturation, Speed}
import org.scalajs.dom.raw.URL

import eu.timepit.refined.auto._
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.scalajs.js.typedarray.TypedArrayBuffer
import scala.concurrent.Future

case class Media(
    id: MediaId,
    name: Option[String],
    mediaType: MediaType,
    data: MediaData,
    brightness: Brightness,
    contrast: Contrast,
    saturation: Saturation,
    speed: Speed,
    size: (Int, Int),
    originalSize: Option[(Int, Int)]
) {
  def fileName() = s"${name.getOrElse("fastrec")}${MediaType.extension(mediaType)}"

  def changeMediaType(dest: MediaType): Future[Media] =
    data match {
      case Ready(blob) =>
        dest match {
          case Mp4() =>
            transcode(Seq("-c:v", "libx264", "-c:a", "copy", "-strict", "experimental"), dest, blob)
          case WebM() => transcode(Seq("-c", "copy"), dest, blob)
          case Avi()  => transcode(Seq("-c", "copy"), dest, blob)
          case Ogg()  => transcode(Seq("-c", "copy"), dest, blob)
          case Mov()  => transcode(Seq("-c", "copy"), dest, blob)
          case Gif()  => transcode(Seq("-r", "10"), dest, blob)
        }
      case _ => Future.successful(this)
    }

  def applyEdit() =
    data match {
      case Ready(blob) =>
        transcode(
          Seq(
            "-vf",
            s"eq=brightness=${brightness.brightness.value}:contrast=${contrast.contrast.value}:saturation=${saturation.saturation.value},setpts=PTS/${speed.speed.value}",
            "-af",
            s"atempo=${speed.speed.value}",
            "-s",
            s"${size._1}x${size._2}",
            "-c:a",
            "copy"
          ),
          mediaType,
          blob
        )
      case _ => Future.successful(this)
    }

  def concat(media: Media) = {

    (this.data, media.data) match {
      case (Ready(blob1), Ready(blob2)) =>
        val ffmpeg       = FFmpeg.createFFmpeg()
        val listFileName = "list.txt"
        val file1        = fileName()
        val file2        = media.fileName()
        val destFileName = s"${name.getOrElse("untitled")}-${media.name.getOrElse("untitled")}"
        val destFile =
          s"${destFileName}${MediaType.extension(media.mediaType)}"

        val listFile      = s"file ${file1}\nfile ${file2}"
        val listBuffer    = new ArrayBuffer(listFile.length)
        val listFileInput = TypedArrayBuffer.wrap(listBuffer)
        listFileInput.put(listFile.getBytes())

        for {
          _ <- ffmpeg.load().toFuture
          _ = ffmpeg.setLogging(true)
          bin1 <- BlobUtil.blobToArrayBuffer(blob1).toFuture
          bin2 <- BlobUtil.blobToArrayBuffer(blob2).toFuture
          _ = ffmpeg.FS(
            "writeFile",
            file1,
            new Uint8Array(bin1)
          )
          _ = ffmpeg.FS(
            "writeFile",
            file2,
            new Uint8Array(bin2)
          )
          _ = ffmpeg.FS("writeFile", listFileName, new Uint8Array(listBuffer, 0, listFile.length))
          _ <- ffmpeg
            .run(Seq("-f", "concat", "-i", listFileName, "-c", "copy", destFile): _*)
            .toFuture
          bytes = ffmpeg.FS("readFile", destFile)
          _     = ffmpeg.FS("unlink", file1)
          _     = ffmpeg.FS("unlink", file2)
          _     = ffmpeg.FS("unlink", listFileName)
          blob  = new Blob(js.Array(bytes), BlobPropertyBag(MediaType.mimeType(mediaType)))
        } yield
          Media(MediaUrl(URL.createObjectURL(blob)), blob)
            .copy(name = Some(destFileName))

      case _ => Future.successful(this)
    }

  }

  private def transcode(options: Seq[String], dest: MediaType, blob: Blob): Future[Media] = {
    data match {
      case Ready(blob) =>
        val ffmpeg       = FFmpeg.createFFmpeg()
        val destFileName = s"converted${MediaType.extension(dest)}"
        for {
          _ <- ffmpeg.load().toFuture
          _ = ffmpeg.setLogging(true)
          bin <- BlobUtil.blobToArrayBuffer(blob).toFuture
          _ = ffmpeg.FS("writeFile", fileName(), new Uint8Array(bin))
          _ <- ffmpeg.run((Seq("-i") :+ fileName()) ++ options :+ destFileName: _*).toFuture;
          bytes = ffmpeg.FS("readFile", destFileName)
          _     = ffmpeg.FS("unlink", fileName())
          blob  = new Blob(js.Array(bytes), BlobPropertyBag(MediaType.mimeType(dest)))
          _     = URL.revokeObjectURL(data.toString())
        } yield
          this.copy(
            mediaType = dest,
            data = Ready(blob)
          )
      case _ =>
        Future.successful(this)
    }
  }
}

object Media {
  def apply(id: MediaId, data: MediaData): Media =
    Media(
      id = id,
      name = None,
      mediaType = Mp4(),
      data = data,
      brightness = Brightness(0.0),
      contrast = Contrast(1),
      saturation = Saturation(1.0),
      speed = Speed(1.0),
      size = (1, 1),
      originalSize = None
    )
  def apply(url: MediaUrl, blob: Blob): Media =
    apply(newId(), Ready(blob))

  def apply(url: MediaUrl, blob: Blob, mediaType: MediaType): Media =
    apply(url, blob).copy(mediaType = mediaType)

  def newId(): MediaId = MediaId(ju.UUID.randomUUID.toString)
}
