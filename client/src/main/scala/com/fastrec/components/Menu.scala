package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import fontAwesome._
import com.fastrec.domain.model.{Media, Ready}
import com.fastrec.facade.PrettyBytes
import com.fastrec.pages.{Page, HomePage, ListPage, EditPage}

object Menu {
  case class Props(
      media: Option[Media],
      page: Page,
      handleClickRecord: Callback,
      handleClickList: Callback,
      handleClickDownload: Callback
  )

  def homeButtonEnabled(page: Page) =
    page match {
      case HomePage => false
      case _        => true
    }

  def listButtonEnabled(page: Page) =
    page match {
      case ListPage => false
      case _        => true
    }

  def downloadButtonEnabled(page: Page) =
    page match {
      case EditPage(_) => true
      case _           => false
    }

  val component = ScalaComponent
    .builder[Props]("Menu")
    .render_P(
      p =>
        <.div(
          ^.className := "menu",
          <.div(
            ^.className := "menu-button",
            <.button(
              ^.className := "button icon-button",
              ^.disabled := !homeButtonEnabled(p.page),
              ^.onClick --> p.handleClickRecord,
              <.div(
                ^.className := "icon",
                ^.dangerouslySetInnerHtml := fontawesome
                  .icon(freeSolid.faVideo)
                  .html
                  .join("")
              )
            ),
            <.div(^.className := "tooltip", <.span(^.className := "text", "Recording"))
          ),
          <.div(
            ^.className := "menu-button",
            <.button(
              ^.className := "button icon-button",
              ^.disabled := !listButtonEnabled(p.page),
              ^.onClick --> p.handleClickList,
              <.div(
                ^.className := "icon",
                ^.dangerouslySetInnerHtml := fontawesome
                  .icon(freeSolid.faList)
                  .html
                  .join("")
              )
            ),
            <.div(^.className := "tooltip", <.span(^.className := "text", "Video List"))
          ),
          <.div(
            ^.className := "menu-button",
            <.button(
              ^.className := "button icon-button",
              ^.disabled := !downloadButtonEnabled(p.page),
              ^.onClick --> p.handleClickDownload,
              <.div(
                ^.className := "icon",
                ^.dangerouslySetInnerHtml := fontawesome
                  .icon(freeSolid.faDownload)
                  .html
                  .join("")
              )
            ),
            p.media
              .map(
                m =>
                  <.div(
                    ^.className := "tooltip",
                    <.span(^.className := "text", m.data match {
                      case Ready(blob) => PrettyBytes.prettyBytes(blob.size)
                      case _           => "0KB"
                    })
                  )
              )
              .getOrElse(<.div())
          )
        )
    )
    .build

  def apply(P: Props) =
    component(P)
}
