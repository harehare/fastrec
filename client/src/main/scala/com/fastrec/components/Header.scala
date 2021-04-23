package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.MediaRecorderStatus
import com.fastrec.domain.model.MediaId
import com.fastrec.pages.{Page, HomePage}

object Header {
  case class Props(
      page: Page,
      status: MediaRecorderStatus,
      canUseCamera: Boolean,
      handleStartRecording: Callback,
      handleStartCameraRecording: Callback,
      handleStopRecording: MediaId => Callback
  )

  val component = ScalaComponent
    .builder[Props]("Controls")
    .render_P(
      p =>
        <.header(
          ^.className := "header",
          p.page match {
            case HomePage => <.div()
            case _        => Text.title("FastREC")
          },
          p.status match {
            case MediaRecorderStatus.Recording =>
              <.div(^.className := "recording", Timer())
            case _ => <.div()
          }
        )
    )
    .build

  def apply(P: Props) =
    component(P)
}
