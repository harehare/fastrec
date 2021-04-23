package com.fastrec.facade

import scala.scalajs.js
import org.scalajs.dom.Blob
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport
import org.scalajs.dom.experimental.mediastream.{MediaStream, MediaStreamConstraints}

@js.native
trait MediaBlob extends js.Object {
  var audio: Blob = js.native
  var video: Blob = js.native
  var gif: Blob   = js.native
}

@js.native
trait Options extends js.Object {
  var `type`: String   = js.native
  var mimeType: String = js.native;
}

@js.native
@JSImport("recordrtc", "RecordRTCPromisesHandler")
class RecordRTC(stream: MediaStream) extends js.Object {
  def startRecording(): Unit           = js.native
  def pauseRecording(): Unit           = js.native
  def resumeRecording(): Unit          = js.native
  def stopRecording(): Promise[String] = js.native;
  def getBlob(): Promise[Blob]         = js.native;
  def getDataURL(): Promise[String]    = js.native;
  def getState(): Promise[String]      = js.native;
}

@js.native
@JSImport("recordrtc", JSImport.Namespace)
object RecordRTC extends js.Object {
  def invokeSaveAsDialog(file: Blob, fileName: String): Unit = js.native;
}

@js.native
trait MediaDevices extends js.Object {
  def getDisplayMedia(
      constraints: MediaStreamConstraints
  ): Promise[MediaStream]
  def getUserMedia(
      constraints: MediaStreamConstraints
  ): Promise[MediaStream]
}

@js.native
trait Navigator extends js.Object {
  val mediaDevices: MediaDevices = js.native
}

object Dom {
  lazy val navigator: Navigator = scala.scalajs.js.Dynamic.global.navigator.asInstanceOf[Navigator]
}
