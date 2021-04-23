package com.fastrec.facade

import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.Uint8Array

@js.native
@JSImport("@ffmpeg/ffmpeg", JSImport.Namespace)
object FFmpeg extends js.Object {
  def createFFmpeg(): FFmpeg = js.native;
}

@js.native
trait Progress extends js.Object {
  val ratio: Float = js.native
}
@js.native
trait FFmpeg extends js.Object {
  def load(): Promise[Unit]                                        = js.native;
  def setLogging(logging: Boolean): Unit                           = js.native;
  def setProgress(progress: Progress => js.Any): Unit              = js.native;
  def FS(option: String, fileName: String, file: Uint8Array): Unit = js.native;
  def FS(option: String, fileName: String): Uint8Array             = js.native;
  def run(options: String*): Promise[Unit]                         = js.native;
}
