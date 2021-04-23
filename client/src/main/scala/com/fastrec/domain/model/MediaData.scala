package com.fastrec.domain.model

import org.scalajs.dom.raw.Blob
import org.scalajs.dom.raw.URL

sealed trait MediaData {
  def get: Blob
  def getOrElse(default: => Blob): Blob =
    this match {
      case Ready(blob) => this.get
      case _           => default
    }
  def url: MediaUrl
}

final case object Empty extends MediaData {
  def get = throw new Exception("Not supported Empty.get")
  def url = MediaUrl("")
}
final case class Processing(progress: Int) extends MediaData {
  def get = throw new Exception("Not supported Processing.get")
  def url = MediaUrl("")
}
final case class Ready(blob: Blob) extends MediaData {
  val u   = MediaUrl(URL.createObjectURL(blob))
  def get = blob
  def url = u
}
