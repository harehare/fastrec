package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.Media
import org.scalajs.dom.html
import com.fastrec.domain.model.{Gif}
import fontAwesome.fontawesome
import fontAwesome.freeSolid

object Video {
  case class Props(
      media: Media,
      handleGetVideoSize: (Int, Int) => Callback,
      handlePrevClick: () => Callback,
      handleNextClick: () => Callback
  )

  case class State(videoTime: Double, paused: Boolean)

  private val videoRef = Ref[html.Video]

  def onLoadedMetadata(p: Props): Callback = {
    videoRef.foreachCB(v => p.handleGetVideoSize(v.videoWidth, v.videoHeight))
  }

  class Backend($ : BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      val controls = <.div(
        ^.className := "video-controls",
        <.button(
          ^.className := "button",
          ^.onClick --> p.handlePrevClick(),
          <.div(
            ^.className := "icon",
            ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faBackward).html.join("")
          )
        ),
        <.button(
          ^.className := "button",
          ^.disabled := !s.paused,
          ^.onClick --> videoRef.foreach(v => v.play()).void,
          <.div(
            ^.className := "icon",
            ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faPlay).html.join("")
          )
        ),
        <.button(
          ^.className := "button",
          ^.disabled := s.paused,
          ^.onClick --> videoRef.foreach(v => v.pause()).void,
          <.div(
            ^.className := "icon",
            ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faPause).html.join("")
          )
        ),
        <.button(
          ^.className := "button",
          ^.onClick --> p.handleNextClick(),
          <.div(
            ^.className := "icon",
            ^.dangerouslySetInnerHtml := fontawesome.icon(freeSolid.faForward).html.join("")
          )
        )
      )

      p.media.mediaType match {
        case Gif() =>
          <.div(
            ^.className := "flex-center full-screen",
            <.img(
              ^.src := p.media.data.url.toString(),
              ^.width := s"${p.media.size._1.toString()}px",
              ^.height := s"${p.media.size._2.toString()}px",
              ^.className := "video"
            )
          )
        case _ =>
          <.div(
            ^.className := "flex-center full-screen",
            <.video(
              ^.className := "video",
              ^.src := p.media.data.url.toString(),
              ^.width := s"min(${p.media.size._1.toString()}px, 100vw)",
              ^.height := s"min(${p.media.size._2.toString()}px, 100vh)",
              ^.playsInline := true,
              ^.loop := true,
              ^.onLoadedMetadata --> onLoadedMetadata(p),
              ^.onLoadedData --> $.modState(
                s => s.copy(videoTime = videoRef.unsafeGet().currentTime)
              ),
              ^.onPlay -->
                $.modState(
                  s => s.copy(paused = false)
                ),
              ^.onPlaying --> videoRef.foreach(v => v.playbackRate = p.media.speed.speed.value),
              ^.onPause -->
                $.modState(s => s.copy(paused = true))
            ).withRef(videoRef),
            controls
          )
      }
    }
  }

  val component = ScalaComponent
    .builder[Props]("Video")
    .initialState(State(0.0, true))
    .renderBackend[Backend]
    .build

  def apply(P: Props) =
    component(P)
}
