package com.pcdn.controller

import com.pcdn.model.Post
import com.pcdn.model.Post._
import com.pcdn.model.utils.{Render, Settings}
import spray.http.MediaTypes.{`text/html` => TEXTHTML}
import spray.routing._
import spray.routing.directives.CachingDirectives
import scala.concurrent.duration._

/**
  * Created by Hung on 8/16/16.
  */

trait SinglePost extends HttpService with Settings with Render with CachingDirectives {
  lazy val singlePost = cache(routeCache(maxCapacity = 100, initialCapacity = 10, timeToIdle = 5.seconds, timeToLive = 60.seconds)) {
    (get & path("_posts" / RestPath)) { id ⇒
      listPost(id.toString) match {
        case Some(p) => render(respondWithMediaType(TEXTHTML), template = html.post.render(Post.buildPost(p)))
        case _ => complete("Not found")
      }
    }
  }
}
