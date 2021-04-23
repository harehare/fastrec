package com.fastrec.domain.model

sealed trait MediaRecorderStatus

object MediaRecorderStatus {
  final case object Inactive  extends MediaRecorderStatus
  final case object Recording extends MediaRecorderStatus
  final case object Paused    extends MediaRecorderStatus
  final case object Stopped   extends MediaRecorderStatus
}
