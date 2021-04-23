package com.fastrec.infrastructure

import scala.scalajs.js
import org.scalajs.dom
import com.fastrec.domain.repository.MediaRepository
import com.fastrec.domain.model.Media
import scala.concurrent.ExecutionContext.Implicits.global
import com.fastrec.domain.model.MediaId
import org.scalajs.dom.raw.IDBDatabase
import org.scalajs.dom.raw.Event
import scala.concurrent.{Future, Promise}
import org.scalajs.dom.raw.IDBObjectStore
import org.scalajs.dom.raw.Blob
import org.scalajs.dom.raw.URL
import com.fastrec.domain.model.{Ready, MediaUrl, MediaType}
import org.scalajs.dom.raw.IDBCursorWithValue

@js.native
trait MediaJs extends js.Object {
  val id: String   = js.native
  val name: String = js.native
  val data: Blob   = js.native
  val width: Int   = js.native
  val height: Int  = js.native
}

object MediaJs {
  def apply(id: String, name: String, data: Blob, width: Int, height: Int): MediaJs =
    js.Dynamic
      .literal(id = id, name = name, data = data, width = width, height = height)
      .asInstanceOf[MediaJs]
}

class IndexedDBRepository extends MediaRepository {
  final val storeName = "FastREC"

  override def findAll(): Future[List[Media]] = {
    withStore { store =>
      val p         = Promise[List[Media]]()
      val req       = store.openCursor()
      var mediaList = List[Media]()
      req.onsuccess = (_: Event) => {
        val row = req.result;
        if (row == null) {
          p.success(mediaList)
        } else {
          val idbValue  = row.asInstanceOf[IDBCursorWithValue]
          val media     = idbValue.value.asInstanceOf[MediaJs]
          val mediaType = MediaType.fromMimeType(media.data.`type`)
          mediaList = Media(
            MediaUrl(URL.createObjectURL(media.data)),
            media.data,
            mediaType
          ).copy(id = MediaId(media.id), name = Some(media.name)) :: mediaList
          idbValue.continue()
        }
      }
      req.onerror = (_: Event) => {
        p.success(Nil)
      }
      p.future
    }
  }

  override def findById(id: MediaId): Future[Option[Media]] = {
    withStore { store =>
      val p   = Promise[Option[Media]]()
      val req = store.get(id.toString())
      req.onsuccess = (_: Event) => {
        val media = req.result.asInstanceOf[js.UndefOr[MediaJs]].toOption
        media match {
          case Some(m) => {
            p.success(
              Some(
                Media(MediaUrl(URL.createObjectURL(m.data)), m.data)
                  .copy(id = MediaId(m.id), name = if (m.name.isEmpty()) {
                    None
                  } else {
                    Some(m.name)
                  })
              )
            )
          }
          case None => p.success(None)
        }
      }
      req.onerror = (_: Event) => {
        p.success(None)
      }
      p.future
    }
  }

  override def add(media: Media): Future[Unit] =
    media.data match {
      case Ready(blob) =>
        withStore { store =>
          val p = Promise[String]()
          val req = store.put(
            MediaJs(
              media.id.toString(),
              media.name.getOrElse("untitled"),
              blob,
              media.size._1,
              media.size._2
            ),
            media.id.toString()
          )
          req.onsuccess = (_: Event) => {
            p.success(req.result.asInstanceOf[String])
          }
          req.onerror = (_: Event) => {
            p.failure(new Exception(s"error ${req.error.name}"))
          }
          p.future.map(_ => ())
        }
      case _ =>
        Future.unit

    }

  override def update(media: Media): Future[Unit] = {
    add(media)
  }

  override def delete(media: Media): Future[Unit] = {
    withStore { store =>
      val p   = Promise[Unit]()
      val req = store.delete(media.id.toString())
      req.onsuccess = (_: Event) => {
        p.success(())
      }
      req.onerror = (_: Event) => {
        p.failure(new Exception(s"error ${req.error.name}"))
      }
      p.future.map(_ => ())
    }
  }

  private lazy val database = {
    if (js.isUndefined(dom.window.indexedDB)) {
      Future.failed(new Exception("not supported"))
    } else {
      val req = dom.window.indexedDB.open("medias")
      val p   = Promise[IDBDatabase]()
      req.onsuccess = (_: Event) => {
        p.success(req.result.asInstanceOf[IDBDatabase])
      }
      req.onupgradeneeded = (_: Event) => {
        val db = req.result.asInstanceOf[IDBDatabase]
        db.createObjectStore(storeName)
      }
      req.onerror = (_: Event) => {
        p.failure(new Exception(s"error ${req.error.name}"))
      }
      p.future
    }
  }

  private def withStore[A](f: IDBObjectStore => Future[A]) = {
    database.flatMap { db =>
      val tx    = db.transaction(storeName, "readwrite")
      val store = tx.objectStore(storeName)
      f(store)
    }
  }
}
