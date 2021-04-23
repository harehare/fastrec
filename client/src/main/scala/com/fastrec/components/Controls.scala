package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.MediaRecorderStatus
import fontAwesome._
import com.fastrec.domain.model.MediaId
import com.fastrec.domain.model.Media

object Controls {
  case class Props(
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
        <.div(
          ^.className := "controls",
          <.button(
            ^.className := "button",
            ^.onClick --> p.handleStartRecording,
            ^.disabled := p.status == MediaRecorderStatus.Recording,
            <.div(
              ^.className := "icon",
              ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faDesktop).html.join("")
            )
          ),
          <.button(
            ^.className := "button",
            ^.onClick --> p.handleStartCameraRecording,
            ^.disabled := p.canUseCamera && p.status == MediaRecorderStatus.Recording,
            <.div(
              ^.className := "icon",
              ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faCamera).html.join("")
            )
          ),
          <.button(
            ^.className := "button",
            ^.onClick --> p.handleStopRecording(Media.newId()),
            ^.disabled := p.status != MediaRecorderStatus.Recording,
            <.div(
              ^.className := "icon",
              ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faStop).html.join("")
            )
          )
        )
    )
    .build

  def apply(P: Props) =
    component(P)
}
