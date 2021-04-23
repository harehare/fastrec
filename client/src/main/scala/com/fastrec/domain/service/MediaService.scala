package com.fastrec.domain.service

import com.fastrec.domain.repository.MediaRepository
import com.fastrec.domain.model.Media
import scala.concurrent.ExecutionContext.Implicits.global
import com.fastrec.domain.model.MediaId

class MediaService(repository: MediaRepository) {
  def findAll() =
    repository.findAll()

  def findById(id: MediaId) =
    repository.findById(id)

  def save(media: Media) =
    for {
      m <- repository.findById(media.id)
      v <- m match {
        case Some(_) => repository.update(media)
        case None    => repository.add(media)
      }
    } yield v

  def delete(media: Media) =
    repository.delete(media)
}
