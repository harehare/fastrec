package com.fastrec.domain.model

import com.fastrec.facade.RecordRTC
import org.scalajs.dom.raw.Blob

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.experimental.mediastream.MediaStream
import com.fastrec.facade.Dom
import org.scalajs.dom.experimental.mediastream.MediaStreamConstraints

class MediaRecorder(stream: MediaStream) {
  val recorder = new RecordRTC(stream)

  def start() = recorder.startRecording()

  def stop(): Future[Media] =
    for {
      url  <- recorder.stopRecording().toFuture
      blob <- recorder.getBlob().toFuture
      _ = stream.getTracks().foreach(t => t.stop())
    } yield Media(MediaUrl(url), blob)

  def state(): Future[MediaRecorderStatus] = {
    for {
      state <- recorder.getState().toFuture
    } yield
      state match {
        case "recording" => MediaRecorderStatus.Recording
        case "paused"    => MediaRecorderStatus.Paused
        case "stopped"   => MediaRecorderStatus.Stopped
        case _           => MediaRecorderStatus.Inactive
      }
  }

  def getBlob(): Future[Blob] = recorder.getBlob().toFuture
}

object MediaRecorder {
  def camera() =
    Dom.navigator.mediaDevices
      .getUserMedia(MediaStreamConstraints(true, true, ""))
      .toFuture
      .map(stream => new MediaRecorder(stream))

  def desktop() =
    Dom.navigator.mediaDevices
      .getDisplayMedia(MediaStreamConstraints(true, true, ""))
      .toFuture
      .map(stream => new MediaRecorder(stream))
}
