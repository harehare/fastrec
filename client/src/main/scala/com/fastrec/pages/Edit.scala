package com.fastrec.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import diode.react._
import com.fastrec.domain.model.MediaId
import japgolly.scalajs.react.extra.router.RouterCtl
import com.fastrec.domain.model.Video
import com.fastrec.components
import com.fastrec.components.{Text, MediaOption}
import java.util.UUID
import com.fastrec.VideoAction
import org.scalajs.dom
import com.fastrec.domain.model.{Processing, MediaType, MediaFilter}

object Edit {
  case class Props(
      page: EditPage,
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Video]
  )

  private val component = ScalaComponent
    .builder[Props]("Edit")
    .render_P(
      p => {
        p.proxy(
          m => {
            val v = m()
            dom.window.onbeforeunload = _ => { if (v.mediaItems.size > 0) "edit" else null }
            v.mediaItems.get(MediaId(p.page.id.toString())) match {
              case Some(media) =>
                <.div(
                  ^.className := "main",
                  media.data match {
                    case Processing(_) =>
                      <.div(
                        ^.className := "processing",
                        <.div(^.className := "loader")
                      )
                    case _ => <.div()
                  },
                  components.Video(
                    components.Video.Props(
                      media = media,
                      handleGetVideoSize = (width: Int, height: Int) =>
                        m.dispatchCB(VideoAction.SetVideoSize(width, height)),
                      handlePrevClick = () => {
                        val index = v.mediaList.lift(v.mediaSelectedIndex - 1) match {
                          case Some(_) =>
                            v.mediaSelectedIndex - 1
                          case None =>
                            v.mediaList.length - 1
                        }
                        (for {
                          mediaId <- v.mediaList.lift(index)
                          media   <- v.mediaItems.get(mediaId)
                        } yield
                          m.dispatchCB(VideoAction.Next(index, Some(media))) >> p.router
                            .set(EditPage(UUID.fromString(mediaId.toString()))))
                          .getOrElse(Callback.empty)
                      },
                      handleNextClick = () => {
                        val index = v.mediaList.lift(v.mediaSelectedIndex + 1) match {
                          case Some(_) =>
                            v.mediaSelectedIndex + 1
                          case None =>
                            0
                        }
                        (for {
                          mediaId <- v.mediaList.lift(index)
                          media   <- v.mediaItems.get(mediaId)
                        } yield
                          m.dispatchCB(VideoAction.Next(index, Some(media))) >> p.router
                            .set(EditPage(UUID.fromString(mediaId.toString()))))
                          .getOrElse(Callback.empty)
                      }
                    )
                  ),
                  MediaOption(
                    MediaOption.Props(
                      media = media,
                      handleChangeMediaType =
                        (mediaType: MediaType) => m.dispatchCB(VideoAction.Convert(mediaType)),
                      handleApply = m.dispatchCB(VideoAction.ApplyEdit),
                      handleReset = m.dispatchCB(VideoAction.Reset),
                      handleGetVideoSize = (width: Int, height: Int) =>
                        m.dispatchCB(VideoAction.SetVideoSize(width, height)),
                      handleChangeBrightness = (brightness: Double) =>
                        m.dispatchCB(
                          VideoAction.SetFilter(
                            MediaFilter.Brightness(brightness).getOrElse(MediaFilter.Brightness())
                          )
                        ),
                      handleChangeContrast = (contrast: Int) =>
                        m.dispatchCB(
                          VideoAction.SetFilter(
                            MediaFilter.Contrast(contrast).getOrElse(MediaFilter.Contrast())
                          )
                        ),
                      handleChangeSaturation = (saturation: Double) =>
                        m.dispatchCB(
                          VideoAction.SetFilter(
                            MediaFilter.Saturation(saturation).getOrElse(MediaFilter.Saturation())
                          )
                        ),
                      handleChangeSpeed = (speed: Double) =>
                        m.dispatchCB(
                          VideoAction
                            .SetFilter(MediaFilter.Speed(speed).getOrElse(MediaFilter.Speed()))
                        )
                    )
                  )
                )

              case None =>
                <.div(
                  ^.className := "center",
                  Text.subTitle("NOT FOUND")
                )
            }
          }
        )
      }
    )
    .build

  def apply(
      page: EditPage,
      router: RouterCtl[Page],
      proxy: ReactConnectProxy[Video]
  ) =
    component(Props(page, router, proxy))
}
