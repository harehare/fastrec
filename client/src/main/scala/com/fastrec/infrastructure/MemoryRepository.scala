package com.fastrec.infrastructure

import com.fastrec.domain.repository.MediaRepository
import com.fastrec.domain.model.Media
import scala.concurrent.Future
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import com.fastrec.domain.model.MediaId

class MemoryRepository extends MediaRepository {

  private[this] val medias = Map[MediaId, Media]()

  override def findAll(): Future[List[Media]] = Future { medias.values.toList }

  override def findById(id: MediaId): Future[Option[Media]] = Future { medias.get(id) }

  override def add(media: Media): Future[Unit] = {
    medias += (media.id -> media)
    Future {}
  }

  override def update(media: Media): Future[Unit] = {
    medias(media.id) = media
    Future {}
  }

  override def delete(media: Media): Future[Unit] = {
    medias.remove(media.id)
    Future {}
  }
}
