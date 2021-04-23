package com.fastrec.domain.model

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._

sealed trait MediaFilter
object MediaFilter {

  type BrightnessRule = Interval.Open[W.`-1.0`.T, W.`1.0`.T]
  type ContrastRule   = Interval.Open[W.`-1000`.T, W.`1000`.T]
  type SaturationRule = Interval.ClosedOpen[W.`0.0`.T, W.`3.0`.T]
  type SpeedRule      = Interval.ClosedOpen[W.`0.1`.T, W.`10.0`.T]

  type BrightnessType = Double Refined BrightnessRule
  type ContrastType   = Int Refined ContrastRule
  type SaturationType = Double Refined SaturationRule
  type SpeedType      = Double Refined SpeedRule

  final case class Brightness(brightness: BrightnessType) extends MediaFilter
  final case class Contrast(contrast: ContrastType)       extends MediaFilter
  final case class Saturation(saturation: SaturationType) extends MediaFilter
  final case class Speed(speed: SpeedType)                extends MediaFilter

  object Brightness {
    def apply(v: Double): Either[String, Brightness] =
      refineV[BrightnessRule](v).map(Brightness(_))

    def apply(): Brightness =
      Brightness(0.0)
  }

  object Contrast {
    def apply(v: Int): Either[String, Contrast] =
      refineV[ContrastRule](v).map(Contrast(_))

    def apply(): Contrast =
      Contrast(0)
  }

  object Saturation {
    def apply(v: Double): Either[String, Saturation] =
      refineV[SaturationRule](v).map(Saturation(_))

    def apply(): Saturation =
      Saturation(1.0)
  }

  object Speed {
    def apply(v: Double): Either[String, Speed] =
      refineV[SpeedRule](v).map(Speed(_))

    def apply(): Speed =
      Speed(1.0)
  }
}
