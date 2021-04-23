package com.fastrec.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import diode.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import com.fastrec.domain.model.Video
import com.fastrec.components.{Text, VideoItem}
import java.util.UUID
import com.fastrec.VideoAction
import fontAwesome._
import japgolly.scalajs.react.component.builder.Lifecycle

object List {
  case class Props(
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Video]
  )

  case class State(searchText: Option[String])

  def handleInput($ : Lifecycle.RenderScope[Props, State, Unit])(e: ReactEventFromInput): Callback =
    $.modState(_.copy(searchText = if (e.target.value.isEmpty()) None else Some(e.target.value)))

  private val component = ScalaComponent
    .builder[Props]("List")
    .initialState(State(None))
    .renderPS(
      ($, p, s) =>
        p.proxy(
          v => {
            val video = v()
            if (video.mediaItems.isEmpty) {
              <.div(
                ^.className := "flex-center full-screen",
                Text.subTitle("Nothing")
              )
            } else {
              <.div(
                ^.className := "main",
                <.div(
                  ^.className := "description",
                  ^.position.relative,
                  ^.width := "100vw",
                  ^.textAlign := "center",
                  "Drag and drop to concatenate videos",
                  <.div(
                    ^.position.absolute,
                    ^.right := "80px",
                    ^.top := "-8px",
                    <.div(
                      ^.position.absolute,
                      ^.left := "8px",
                      ^.top := "8px",
                      ^.color := "#000",
                      ^.dangerouslySetInnerHtml := fontawesome
                        .icon(freeSolid.faSearch)
                        .html
                        .join("")
                    ),
                    <.input(
                      ^.className := "search",
                      ^.placeholder := "Search",
                      ^.onInput ==> handleInput($)
                    )
                  )
                ),
                <.div(
                  ^.className := "video-list",
                  ^.onDragOver ==> ((e: ReactDragEvent) => {
                    e.stopPropagation()
                    e.preventDefault()
                    Callback.empty
                  }),
                  ^.onDragEnter ==> ((e: ReactDragEvent) => {
                    e.stopPropagation()
                    e.preventDefault()
                    Callback.empty
                  }),
                  ^.onDrop ==> ((e: ReactDragEvent) => {
                    e.stopPropagation()
                    e.preventDefault()
                    val files = e.dataTransfer.files
                    if (files.length > 0) {
                      v.dispatchCB(VideoAction.OpenFile(files.item(0)))
                    } else {
                      Callback.empty
                    }
                  }),
                  video.mediaItems.values
                    .filter(
                      m =>
                        (for {
                          name       <- m.name
                          searchText <- s.searchText
                          hasText    <- Some(name.toLowerCase().contains(searchText.toLowerCase()))
                        } yield hasText).getOrElse(true)
                    )
                    .toTagMod(
                      m =>
                        VideoItem(
                          VideoItem
                            .Props(
                              media = m,
                              handleClick = p.router
                                .set(EditPage(UUID.fromString(m.id.toString()))) >> v
                                .dispatchCB(
                                  VideoAction.SelectMedia(Some(m))
                                ),
                              handleCloseClick = v.dispatchCB(VideoAction.DeleteMedia(m)),
                              handleDrop = (mediaId1, mediaId2) =>
                                v.dispatchCB(VideoAction.Concat(mediaId1, mediaId2)),
                              handleEditTitle =
                                (media, title) => v.dispatchCB(VideoAction.EditTitle(media, title)),
                              handleEndEditTitle =
                                (media) => v.dispatchCB(VideoAction.EndEditTitle(media))
                            )
                        )
                    )
                )
              )
            }
          }
        )
    )
    .build

  def apply(
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Video]
  ) =
    component(Props(router, proxy))

}
