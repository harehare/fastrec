package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Text {

  val logo = ScalaComponent
    .builder[String]
    .render_P(
      title =>
        <.div(
          ^.className := "logo",
          <.img(^.className := "img", ^.src := "/icon.svg"),
          <.div(^.className := "text", title)
        )
    )
    .build

  val title = ScalaComponent
    .builder[String]
    .render_P(
      title =>
        <.div(
          ^.className := "header-title",
          <.img(^.className := "img", ^.src := "/icon.svg"),
          <.div(^.className := "text", title)
        )
    )
    .build

  val subTitle = ScalaComponent
    .builder[String]
    .render_P(
      title =>
        <.div(
          ^.className := "logo",
          <.img(^.className := "sub-img", ^.src := "/icon.svg"),
          <.div(^.className := "sub-text", title)
        )
    )
    .build

  val description = (description: String) =>
    <.div(
      ^.className := "description",
      ^.padding := "8px",
      ^.textAlign := "center",
      description
    )
}
