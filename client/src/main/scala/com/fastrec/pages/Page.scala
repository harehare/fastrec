package com.fastrec.pages

import java.util.UUID
import japgolly.scalajs.react.extra.router.RouterCtl
import com.fastrec.AppCircuit
import com.fastrec.components.{Controls, Menu}
import com.fastrec.{RecorderAction, VideoAction}
import com.fastrec.components.Header

sealed trait Page
case object HomePage          extends Page
case object ListPage          extends Page
case class EditPage(id: UUID) extends Page

object Page {
  private val recorderConnection = AppCircuit.connect(_.recorder)
  private val videoConnection    = AppCircuit.connect(_.video)

  def header(page: Page, ctl: RouterCtl[Page]) =
    recorderConnection(p => {
      val recorder = p()
      Header(
        Header
          .Props(
            page = page,
            status = recorder.status,
            // TODO:
            canUseCamera = true,
            handleStartRecording = p.dispatchCB(RecorderAction.Start),
            handleStartCameraRecording = p.dispatchCB(RecorderAction.StartCamera),
            handleStopRecording = (id) =>
              ctl.set(EditPage(UUID.fromString(id.toString()))) >> p.dispatchCB(
                RecorderAction.Stop(id)
              )
          )
      )
    })

  def controls(ctl: RouterCtl[Page]) =
    recorderConnection(p => {
      val recorder = p()
      Controls(
        Controls
          .Props(
            status = recorder.status,
            // TODO:
            canUseCamera = true,
            handleStartRecording = p.dispatchCB(RecorderAction.Start),
            handleStartCameraRecording = p.dispatchCB(RecorderAction.StartCamera),
            handleStopRecording = (id) =>
              ctl.set(EditPage(UUID.fromString(id.toString()))) >> p.dispatchCB(
                RecorderAction.Stop(id)
              )
          )
      )
    })

  val menu = (
      ctl: RouterCtl[Page],
      page: Page
  ) =>
    videoConnection(
      v =>
        Menu(
          Menu.Props(
            media = v().editMedia,
            page = page,
            handleClickList = ctl.set(ListPage),
            handleClickRecord = ctl.set(HomePage) >> v.dispatchCB(VideoAction.SelectMedia(None)),
            handleClickDownload = v.dispatchCB(VideoAction.Download)
          )
        )
    )
}
