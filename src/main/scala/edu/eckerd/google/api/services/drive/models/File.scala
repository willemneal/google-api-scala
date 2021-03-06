package edu.eckerd.google.api.services.drive.models

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.{File => jFile}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


import scala.language.implicitConversions


/**
  * Created by davenpcm on 5/5/16.
  */
sealed trait File{
  val name: String
  val id: String
  val mimeType: String
}

object File {

  implicit class AsJavaFile(file: File){
    def asJava: jFile = file match {
      case FileMetaData(id, name, mimeType) =>
        new jFile()
          .setId(id)
          .setName(name)
          .setMimeType(mimeType)
      case CompleteFile(id, name, mimeType, size, createdTime, modifiedTime, parentIds, trashed)=>
        new jFile()
          .setId(id)
          .setName(name)
          .setMimeType(mimeType)
          .setCreatedTime(createdTime)
          .setModifiedTime(modifiedTime)
          .setParents{ import scala.collection.JavaConverters._ ; parentIds.asJava}
          .setTrashed(trashed)
          .setSize(size)

    }
  }

  implicit class AsScalaFile(file: jFile){
    def asScala: File = {
      val idOpt = Option(file.getId)
      val nameOpt = Option(file.getName)
      val mimeTypeOpt = Option(file.getMimeType)
      val createdTimeOpt = Option(file.getCreatedTime)
      val modifiedTimeOpt = Option(file.getModifiedTime)
      val trashedOpt = Option(file.getTrashed)

      val parentIdsOpt = Option(file.getParents).map{import scala.collection.JavaConverters._; _.asScala.toList}
      val sizeOpt: Option[Long] = Option(file.getSize).map(_.toLong)

      (
        idOpt,
        nameOpt,
        mimeTypeOpt,
        createdTimeOpt,
        modifiedTimeOpt,
        trashedOpt
      ) match {
        case (
          Some(id),
          Some(name),
          Some(mimeType),
          Some(createdTime),
          Some(modifiedTime),
          Some(trashed)
          ) =>
          CompleteFile(
            id,
            name,
            mimeType,
            sizeOpt.getOrElse(0L),
            createdTime,
            modifiedTime,
            parentIdsOpt.getOrElse(List[String]()),
            trashed
          )

        case (
          Some(id),
          Some(name),
          Some(mimeType),
          _, _, _
          ) =>
          FileMetaData(id, name, mimeType)
        case _ => throw new Error("File Should Always Return ID, Name, and MimeType")
      }
    }

  }

  val rfc3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  // 2015-08-05T19:40:52.601Z
  implicit private[this] def convertGoogleTimeToJavaTime(dateTime: DateTime): LocalDateTime = {
    LocalDateTime.parse(dateTime.toStringRfc3339, rfc3339)
  }
  implicit private[this] def convertJavaTimetoGoogleTime(localDateTime: LocalDateTime): DateTime = {
    DateTime.parseRfc3339(localDateTime.format(rfc3339))
  }
}

case class FileMetaData(
                         id: String,
                         name: String,
                         mimeType: String
                       ) extends File


case class CompleteFile(
                       id: String,
                       name: String,
                       mimeType: String,
                       size: Long,
                       createdTime: LocalDateTime,
                       modifiedTime: LocalDateTime,
                       parentIds: List[String],
                       trashed: Boolean
                       ) extends File

//case class File(name: String,
//                mimeType: String,
//                id: String,
//                createdTime: Option[DateTime],
//                modifiedTime: Option[DateTime],
//                trashed: Option[Boolean],
//                parentIds: Option[List[String]] = None,
//                content: Option[FileContent]= None
//               )