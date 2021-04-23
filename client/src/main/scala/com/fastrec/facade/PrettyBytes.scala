package com.fastrec.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object PrettyBytes {
  @js.native
  @JSImport("pretty-bytes", JSImport.Default)
  def prettyBytes(byte: Double): String = js.native;
}
