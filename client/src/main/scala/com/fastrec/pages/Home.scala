package com.fastrec.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import diode.react._
import com.fastrec.domain.model.MediaRecorderStatus
import japgolly.scalajs.react.extra.router.RouterCtl
import com.fastrec.domain.model.Recorder
import com.fastrec.components.Text
import com.fastrec.domain.model.Video
import com.fastrec.RecorderAction

object Home {
  case class Props(
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Recorder],
      videoProxy: ReactConnectProxy[Video]
  )

  private val component = ScalaComponent
    .builder[Props]("Home")
    .render_P(
      p =>
        <.div(
          ^.className := "main",
          Text.logo("FastREC"),
          Text.description("Fast capture your tab or webcam. Works only on the your browser"),
          p.proxy(
            r => {
              val recorder = r()
              recorder.status match {
                case MediaRecorderStatus.Recording =>
                  <.div(^.className := "recording", <.div(^.className := "loader"))
                case _ =>
                  <.div(
                    ^.className := "title",
                    <.input(
                      ^.`type` := "text",
                      ^.className := "input",
                      ^.placeholder := "Enter a title",
                      ^.value := recorder.title.getOrElse(""),
                      ^.onInput ==> (
                          (e: ReactEventFromInput) =>
                            r.dispatchCB(RecorderAction.EditTitle(e.target.value))
                        )
                    )
                  )
              }
            }
          ),
          Page.controls(p.router)
        )
    )
    .build

  def apply(
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Recorder],
      videoProxy: ReactConnectProxy[Video]
  ) =
    component(Props(router, proxy, videoProxy))

}
