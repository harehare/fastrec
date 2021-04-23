package com.fastrec.domain.model


case class Recorder(
    title: Option[String],
    recorder: Option[MediaRecorder],
    status: MediaRecorderStatus,
    recordingTime: Option[Int]
)

case class Video(
    editMediaId: Option[MediaId],
    baseMedia: Option[Media],
    mediaItems: Map[MediaId, Media],
    mediaList: List[MediaId],
    mediaSelectedIndex: Int
) {
  def editMedia =
    for {
      id    <- editMediaId
      media <- mediaItems.get(id)
    } yield media
}

case class Model(
    recorder: Recorder,
    video: Video
)
