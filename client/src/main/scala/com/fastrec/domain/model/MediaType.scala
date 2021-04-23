package com.fastrec.domain.model

import cats.Show
import cats.implicits._

sealed trait MediaType
final case class Mp4()  extends MediaType
final case class WebM() extends MediaType
final case class Avi()  extends MediaType
final case class Ogg()  extends MediaType
final case class Mov()  extends MediaType
final case class Gif()  extends MediaType

object MediaType {

  def list() =
    List(
      Mp4().show,
      WebM().show,
      Avi().show,
      Ogg().show,
      Mov().show,
      Gif().show
    )

  def extension(mediaType: MediaType) = mediaType match {
    case WebM() => ".webm"
    case Mp4()  => ".mp4"
    case Avi()  => ".avi"
    case Ogg()  => ".ogg"
    case Mov()  => ".mov"
    case Gif()  => ".gif"
  }

  def mimeType(mediaType: MediaType) = mediaType match {
    case WebM() => "video/webm"
    case Mp4()  => "video/mp4"
    case Avi()  => "video/x-msvideo"
    case Ogg()  => "video/ogg"
    case Mov()  => "video/quicktime"
    case Gif()  => "image/gif"
  }

  def show(mediaType: MediaType) = mediaType match {
    case WebM() => WebM().show
    case Mp4()  => Mp4().show
    case Avi()  => Avi().show
    case Ogg()  => Ogg().show
    case Mov()  => Mov().show
    case Gif()  => Gif().show
  }

  def fromString(s: String) = s match {
    case "webm" => WebM()
    case "mp4"  => Mp4()
    case "avi"  => Avi()
    case "ogg"  => Ogg()
    case "mov"  => Mov()
    case "gif"  => Gif()
    case _      => WebM()
  }

  def fromFileName(s: String) = s.split(".") match {
    case Array(_, "webm") => WebM()
    case Array(_, "mp4")  => Mp4()
    case Array(_, "avi")  => Avi()
    case Array(_, "ogg")  => Ogg()
    case Array(_, "mov")  => Mov()
    case Array(_, "gif")  => Gif()
    case _                => Mp4()
  }

  def fromMimeType(mimeType: String) = mimeType.split(";") match {
    case Array("video/x-matroska", _) => Mp4()
    case Array("video/mp4", _)        => Mp4()
    case Array("video/webm", _)       => WebM()
    case Array("video/x-msvideo", _)  => Avi()
    case Array("video/ogg", _)        => Ogg()
    case Array("video/quicktime", _)  => Mov()
    case Array("image/gif", _)        => Gif()
    case _                            => Mp4()
  }

  implicit val showMp4: Show[Mp4] = Show.show(
    _ => "mp4"
  )

  implicit val showWebM: Show[WebM] = Show.show(
    _ => "WebM"
  )

  implicit val showAvi: Show[Avi] = Show.show(
    _ => "Avi"
  )

  implicit val showOgg: Show[Ogg] = Show.show(
    _ => "Ogg"
  )

  implicit val showMov: Show[Mov] = Show.show(
    _ => "Mov"
  )

  implicit val showGif: Show[Gif] = Show.show(
    _ => "gif"
  )
}
