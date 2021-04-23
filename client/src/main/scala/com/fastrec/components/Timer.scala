package com.fastrec.components

import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Timer {

  case class State(hour: Int, minute: Int, second: Int)

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State): VdomElement = {
      <.div(
        ^.className := "timer",
        <.div(^.className := "circle", ^.opacity := (if (s.second % 2 == 0) "1" else "0")),
        <.div(^.className := "hour", s"00${s.hour}".takeRight(2)),
        ":",
        <.div(^.className := "minute", s"00${s.minute}".takeRight(2)),
        ":",
        <.div(^.className := "second", s"00${s.second}".takeRight(2))
      )
    }

    var interval: js.UndefOr[js.timers.SetIntervalHandle] =
      js.undefined

    def tick =
      $.modState(
        s =>
          State(
            hour = if (s.minute + 1 >= 60) s.hour + 1 else s.hour,
            if (s.second + 1 >= 60) s.minute + 1 else s.minute,
            if (s.second + 1 >= 60) 0
            else s.second + 1
          )
      )

    def start = Callback {
      interval = js.timers.setInterval(1000)(tick.runNow())
    }

    def clear = Callback {
      interval foreach js.timers.clearInterval
      interval = js.undefined
    }
  }

  val component = ScalaComponent
    .builder[Unit]("Timer")
    .initialState(State(0, 0, 0))
    .renderBackend[Backend]
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.clear)
    .build

  def apply() =
    component()
}
