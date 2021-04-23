package com.fastrec.domain

import io.estatico.newtype.macros.newtype
import scala.language.implicitConversions

package object model {
  @newtype case class MediaId(id: String)
  @newtype case class MediaUrl(url: String)
}
