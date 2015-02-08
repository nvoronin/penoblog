package controllers

import models.BlogPost
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

object Blog extends Controller with Secured{

  val pageSize = 5;

  val blogPostForm = Form(tuple(
    "handler" -> nonEmptyText,
    "title" -> nonEmptyText,
    "summary" -> nonEmptyText,
    "content" -> nonEmptyText,
    "tags" -> nonEmptyText
  ))

  def list(page: Int) = Action { implicit request =>
    Ok(views.html.blogList(BlogPost.byPage(page - 1, pageSize), page, BlogPost.totalBlogPosts / pageSize))
  }

  def render(handle: String) = Action { implicit request =>
    val result = BlogPost.byHandle(handle);
    if (result.isEmpty)
      Redirect(routes.Blog.list(1))
    else
      Ok(views.html.blogPost(result.head))
  }

  def newPost = withAuth { username => implicit request =>
    blogPostForm.bindFromRequest.fold(
      errors => BadRequest(views.html.addBlogPost(errors)),
      tuple => {
        BlogPost.create(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5.split(",").map(s => s.trim))
        Redirect(routes.Blog.list(1))
      }
    )
  }

  def delete(id: String) = withAuth { username => implicit request =>
    Redirect(routes.Blog.list(1))
  }

}