package com.pcdn.model


import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import akka.http.scaladsl.model.DateTime
import com.pcdn.model.github.{BlogMetadata, Tags}
import com.pcdn.model.utils.{Markdown, Settings}
import play.twirl.api.Html

import scala.io.Source
import scala.language.postfixOps

/**
  * Created by Hung on 8/16/16.
  */
object Post extends Settings {
  private final val TITLE_REGEX = "TITLE\\:.*".r
  private final val DESC_REGEX = "DESCRIPTION\\:.*".r
  private final val TAGS_REGEX = "TAGS\\:.*".r

  def listPost(id: String): Option[File] = {
    new File(dataDir + "/_posts").listFiles.find({
      x => x.getName.replaceAll(".md$", "") == id && x.isFile
    })
  }

  def listPostFromDb(page: Int): List[BlogMetadata] = {
    // Github api return newest commit then the latter, so newest key will be at the last
    BlogMetadata.listAll().sortWith(_.updateTime > _.updateTime)
  }

  def getTitle(input: String): String = TITLE_REGEX.findFirstIn(input) match {
    case None => "Untitled"
    case Some(t) => t.replace("TITLE:", "")
  }

  def getDesc(input: String): String = DESC_REGEX.findFirstIn(input) match {
    case None => ""
    case Some(d) => d.replace("DESCRIPTION:", "")
  }

  def getTags(input: String): Option[String] = TAGS_REGEX.findFirstIn(input) match {
    case None => None
    case Some(t) => Some(t.replace("TAGS:", ""))
  }

  def buildPost(f: File): Post = {
    val resource = Source.fromFile(f, "UTF-8")
    val src: Iterator[String] = resource.getLines()
    val title = src drop 0 next
    val desc = src drop 0 next
    // TODO: markup html for tag, for now just remove tags from post
    val tags = {
      src drop 0 next
    }.replace("TAGS:", "").split(",").map(_.trim).filter(_!="").toList
    var (date, author) = (DateTime.now.toIsoDateString, "Anonymous")
    val content: Html = Html(Markdown(5000).parseToHTML(src mkString "\n"))
    BlogMetadata.get("_posts/" + f.getName) match {
      case Some(x) =>
        date = getPostDay(x.updateTime)
        author = x.author
      case _ => ()
    }
    resource.close()
    Post(title.replace("TITLE:", ""), desc.replace("DESCRIPTION:", ""), content.toString, date, author, tags)
  }

  def getPostDay(date: String): String = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault).parse(date).toString

  def retrieveTagInfo(tagStr: String): List[BlogMetadata] = Tags.get(tagStr) match {
    case Some(p) =>
      p.map(BlogMetadata.get).filter(_.isDefined).map {
        case Some(m) => m
      }.toList
    case _ => Nil
  }
}

case class Post(title: String, desc: String, content: String, date: String, author: String, tags: List[String])
