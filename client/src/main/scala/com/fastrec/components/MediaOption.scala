package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.{Media, MediaType}
import cats.implicits._

object MediaOption {

  private[this] val scaleList = List(25, 50, 100, 200, 300, 400)

  case class Props(
      media: Media,
      handleChangeMediaType: MediaType => Callback,
      handleChangeBrightness: Double => Callback,
      handleChangeContrast: Int => Callback,
      handleChangeSaturation: Double => Callback,
      handleChangeSpeed: Double => Callback,
      handleApply: Callback,
      handleReset: Callback,
      handleGetVideoSize: (Int, Int) => Callback
  )

  case class State(scale: Int)

  def handleChangeMediaType(p: Props)(e: ReactEventFromInput): Callback =
    p.handleChangeMediaType(MediaType.fromString(e.target.value))

  class Backend($ : BackendScope[Props, State]) {
    def toInt(s: String): Option[Int] =
      try {
        Some(s.toInt)
      } catch {
        case _: NumberFormatException => None
      }

    def handleChangeScale(p: Props)(e: ReactEventFromInput): Callback = {
      val scale = e.target.value.toFloat / 100
      p.handleGetVideoSize(
        math.round(p.media.originalSize.getOrElse((0, 0))._1.toFloat * scale),
        math.round(p.media.originalSize.getOrElse((0, 0))._2.toFloat * scale)
      ) >>
        $.modState(_.copy(scale = e.target.value.toInt))
    }

    def handleChangeEvent(f: Double => Callback)(e: ReactEventFromInput): Callback =
      f(e.target.value.toDouble / 10.0)

    def handleChangeIntEvent(f: Int => Callback)(e: ReactEventFromInput): Callback =
      f(e.target.value.toInt)

    def widthChanged(p: Props)(e: ReactEventFromInput): Callback =
      p.handleGetVideoSize(toInt(e.target.value).getOrElse(p.media.size._1), p.media.size._2)

    def heightChanged(p: Props)(e: ReactEventFromInput): Callback =
      p.handleGetVideoSize(p.media.size._1, toInt(e.target.value).getOrElse(p.media.size._2))

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.className := "option",
        <.div(
          ^.className := "section",
          <.div(^.className := "title", ^.borderTopLeftRadius := "8px", "Edit"),
          <.div(
            ^.className := "section",
            <.div(
              ^.maxHeight := "240px",
              ^.overflowY := "scroll",
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Preset:"),
                <.select(
                  ^.className := "select",
                  ^.margin := "0px",
                  ^.width := "50%",
                  ^.value := s.scale,
                  ^.onChange ==> handleChangeScale(p),
                  scaleList.toTagMod(v => <.option(^.value := v, s"${v}%"))
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Width:"),
                <.input(
                  ^.`type` := "number",
                  ^.className := "value",
                  ^.value := p.media.size._1.show,
                  ^.onInput ==> widthChanged(p)
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Height:"),
                <.input(
                  ^.`type` := "number",
                  ^.className := "value",
                  ^.value := p.media.size._2.show,
                  ^.onInput ==> heightChanged(p)
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Brightness:"),
                <.input(
                  ^.`type` := "range",
                  ^.backgroundColor := "transparent",
                  ^.max := "10.0",
                  ^.min := "-10.0",
                  ^.step := "0.1",
                  ^.className := "value",
                  ^.value := (p.media.brightness.brightness.value * 10.0).toString(),
                  ^.onInput ==> handleChangeEvent(p.handleChangeBrightness)
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Saturation:"),
                <.input(
                  ^.`type` := "range",
                  ^.backgroundColor := "transparent",
                  ^.max := "30.0",
                  ^.min := "0.0",
                  ^.step := "1",
                  ^.className := "value",
                  ^.value := (p.media.saturation.saturation.value * 10.0).toString(),
                  ^.onInput ==> handleChangeEvent(p.handleChangeSaturation)
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Contrast:"),
                <.input(
                  ^.`type` := "range",
                  ^.backgroundColor := "transparent",
                  ^.max := "1000",
                  ^.min := "-1000",
                  ^.step := "1",
                  ^.className := "value",
                  ^.value := p.media.contrast.contrast.value.toString(),
                  ^.onInput ==> handleChangeIntEvent(p.handleChangeContrast)
                )
              ),
              <.div(
                ^.className := "form",
                <.div(^.className := "label", "Speed:"),
                <.input(
                  ^.`type` := "range",
                  ^.backgroundColor := "transparent",
                  ^.max := "100",
                  ^.min := "1",
                  ^.step := "1",
                  ^.className := "value",
                  ^.value := (p.media.speed.speed.value * 10).toString(),
                  ^.onInput ==> handleChangeEvent(p.handleChangeSpeed)
                )
              )
            ),
            <.div(
              ^.className := "buttons",
              <.button(
                ^.className := "button reset-button",
                "Reset",
                ^.onClick --> p.handleReset
              ),
              <.button(
                ^.className := "button apply-button",
                "Apply",
                ^.onClick --> p.handleApply
              )
            )
          )
        ),
        <.div(
          ^.className := "section",
          <.div(^.className := "title", "Convert"),
          <.div(
            ^.className := "section",
            ^.width := "100%",
            <.select(
              ^.className := "select",
              ^.value := MediaType.show(p.media.mediaType).toLowerCase(),
              ^.onChange ==> handleChangeMediaType(p),
              MediaType.list().toTagMod(v => <.option(^.value := v.toLowerCase(), v))
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("MediaOption")
    .initialState(State(100))
    .renderBackend[Backend]
    .build

  def apply(P: Props) =
    component(P)
}
