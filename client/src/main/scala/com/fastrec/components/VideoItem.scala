package com.fastrec.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import com.fastrec.domain.model.Media
import org.scalajs.dom.html
import com.fastrec.domain.model.{MediaType, Gif, MediaId, Processing}
import fontAwesome._

object VideoItem {

  case class Props(
      media: Media,
      handleClick: Callback,
      handleCloseClick: Callback,
      handleDrop: (MediaId, MediaId) => Callback,
      handleEditTitle: (Media, String) => Callback,
      handleEndEditTitle: (Media) => Callback
  )

  case class State(drop: Boolean, editTitle: Boolean)

  def handleDragStart(p: Props)(e: ReactDragEvent): Callback = {
    e.dataTransfer.setData(MediaType.mimeType(p.media.mediaType), p.media.id.toString())
    e.dataTransfer.dropEffect = "link"
    e.stopPropagationCB
  }

  class Backend($ : BackendScope[Props, State]) {
    private val videoRef = Ref[html.Video]

    def handleDrop(p: Props)(e: ReactDragEvent): Callback = {
      val mediaId = e.dataTransfer.getData(MediaType.mimeType(p.media.mediaType))
      if (mediaId == null || mediaId.isEmpty() || p.media.id.toString() == mediaId) {
        Callback.empty
      } else {
        $.modState(s => s.copy(drop = false)) >> e.preventDefaultCB >> p.handleDrop(
          p.media.id,
          MediaId(mediaId)
        )

      }
    }

    def handleEditTitle(p: Props)(e: ReactEventFromInput): Callback =
      p.handleEditTitle(p.media, e.target.value)

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.className := "video-item-container",
        ^.position := "relative",
        ^.padding := "8px",
        ^.draggable := true,
        ^.className := (if (s.drop) "video-drop" else ""),
        ^.onDragStart ==> handleDragStart(p),
        ^.onDragOver ==> ((e: ReactDragEvent) => e.preventDefaultCB),
        ^.onDragEnter --> $.modState(s => s.copy(drop = true)),
        ^.onDragLeave --> $.modState(s => s.copy(drop = false)),
        ^.onDrop ==> handleDrop(p),
        <.div(
          ^.className := "icon close-button",
          ^.onClick --> p.handleCloseClick,
          ^.dangerouslySetInnerHtml := fontawesome
            .icon(freeSolid.faTimes)
            .html
            .join("")
        ),
        p.media.mediaType match {
          case Gif() =>
            <.img(
              ^.className := "video-item",
              ^.src := p.media.data.url.toString(),
              ^.onClick --> p.handleClick
            )
          case _ =>
            <.video(
              ^.className := "video-item",
              ^.src := p.media.data.url.toString(),
              ^.muted := true,
              ^.loop := true,
              ^.onClick --> p.handleClick,
              ^.onMouseEnter --> videoRef.foreach(v => v.play()).void,
              ^.onMouseLeave --> videoRef.foreach(v => v.pause()).void
            ).withRef(videoRef)
        },
        if (s.editTitle) {
          <.div(
            ^.className := "video-title",
            <.input(
              ^.className := "video-title video-input",
              ^.marginTop := "2px",
              ^.value := p.media.name.getOrElse(""),
              ^.autoFocus := true,
              ^.onInput ==> handleEditTitle(p),
              ^.onBlur --> $.modState(
                s => s.copy(editTitle = !s.editTitle)
              ),
              ^.onKeyDown ==> ((e: ReactKeyboardEventFromHtml) => {
                if (e.keyCode == 13) {
                  $.modState(
                    s => s.copy(editTitle = !s.editTitle)
                  ) >> p.handleEndEditTitle(p.media)
                } else {
                  Callback.empty
                }
              })
            )
          )
        } else {
          <.div(
            ^.className := "video-title",
            ^.marginTop := "8px",
            ^.marginLeft := "2px",
            p.media.name.getOrElse("untitled").asInstanceOf[String],
            ^.onClick --> $.modState(
              s => s.copy(editTitle = !s.editTitle)
            )
          )
        },
        p.media.data match {
          case Processing(_) =>
            <.div(^.className := "loader video-status")
          case _ => <.div()
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("VideoItem")
    .initialState(State(false, false))
    .renderBackend[Backend]
    .build

  def apply(P: Props) =
    component(P)
}
