package com.fastrec

import com.softwaremill.macwire._
import com.fastrec.domain.service.MediaService
import com.fastrec.infrastructure.IndexedDBRepository

trait AppModule {
  lazy val memoryRepository = wire[IndexedDBRepository]
  lazy val mediaService     = wire[MediaService]
}
