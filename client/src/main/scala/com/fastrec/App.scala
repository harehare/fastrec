package com.fastrec

import japgolly.scalajs.react.extra.router.{BaseUrl, Resolution, Router, RouterConfigDsl, RouterCtl}

import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.MediaId
import com.fastrec.pages.{Page, HomePage, ListPage, EditPage}
import org.scalajs.dom
import japgolly.scalajs.react.vdom.VdomElement
import com.fastrec.pages
import scala.concurrent.ExecutionContext.Implicits.global

object App extends AppModule {
  private val recorderConnection = AppCircuit.connect(_.recorder)
  private val videoConnection    = AppCircuit.connect(_.video)
  private val config = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._
    import japgolly.scalajs.react.extra.router.SetRouteVia
    (emptyRule
      | staticRoute(root, HomePage) ~> renderR(pages.Home(_, recorderConnection, videoConnection))
      | staticRoute("list", ListPage) ~> renderR(pages.List(_, videoConnection))
      | dynamicRouteCT(
        ("edit" / uuid)
          .caseClass[EditPage]
      ) ~> dynRenderR[EditPage, VdomElement](
        (editPage, ctl) => {
          mediaService
            .findById(MediaId(editPage.id.toString()))
            .map(m => AppCircuit.dispatch(VideoAction.SelectMedia(m)))
          pages.Edit(editPage, ctl, videoConnection)
        }
      ))
      .notFound(redirectToPage(HomePage)(SetRouteVia.HistoryReplace))
      .setTitle(
        p =>
          p match {
            case _ => "FastREC"
          }
      )
      .renderWith(layout)
  }

  private def layout(ctl: RouterCtl[Page], r: Resolution[Page]) = {
    <.div(
      ^.className := "container",
      Page.header(r.page, ctl),
      <.div(
        ^.display := "flex",
        ^.width := "100vw",
        ^.height := "calc(100vh - var(--header-height))",
        Page.menu(ctl, r.page),
        r.render()
      )
    )
  }

  def main(args: Array[String]): Unit = {
    mediaService.findAll().map(m => AppCircuit.dispatch(VideoAction.LoadMedia(m)))
    val router = Router(BaseUrl.fromWindowOrigin_/, config)
    router().renderIntoDOM(dom.document.getElementById("main"))
  }
}
