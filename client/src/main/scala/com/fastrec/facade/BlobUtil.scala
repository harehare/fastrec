package com.fastrec.facade

import scala.scalajs.js
import org.scalajs.dom.Blob
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.ArrayBuffer

@js.native
@JSImport("blob-util", JSImport.Namespace)
object BlobUtil extends js.Object {
  def blobToArrayBuffer(blob: Blob): Promise[ArrayBuffer] = js.native;
}
