package com.fastrec.domain.repository

import com.fastrec.domain.model.Media
import scala.concurrent.Future
import com.fastrec.domain.model.MediaId

trait MediaRepository {
  def findAll(): Future[List[Media]]
  def findById(id: MediaId): Future[Option[Media]]
  def add(media: Media): Future[Unit]
  def update(media: Media): Future[Unit]
  def delete(media: Media): Future[Unit]
}
