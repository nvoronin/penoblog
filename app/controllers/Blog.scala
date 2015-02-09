package controllers

import models.BlogPost
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.data.format.Formats._

object Blog extends Controller with Secured {

  val pageSize = 5;

  /*val blogPostForm = Form(tuple(
    "handler" -> nonEmptyText,
    "title" -> nonEmptyText,
    "summary" -> nonEmptyText,
    "content" -> nonEmptyText,
    "tags" -> nonEmptyText
  ))*/

  val blogPostForm = Form(
    mapping(
      "id" -> optional(of[Long]),
      "handler" -> nonEmptyText,
      "title" -> nonEmptyText,
      "summary" -> nonEmptyText,
      "content" -> nonEmptyText,
      "published" -> optional(jodaDate),
      "tags" -> nonEmptyText
    )(BlogPost.apply)(BlogPost.unapply)
  )

  def list(page: Int) = Action { implicit request =>
    Ok(views.html.blogList(BlogPost.byPage(page - 1, pageSize), page,
      BlogPost.totalBlogPosts / pageSize, !username(request).isEmpty))
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
        if(tuple.id.isDefined)
          BlogPost.update(tuple)
        else
          BlogPost.create(tuple)
        Redirect(routes.Blog.list(1))
      }
    )
  }

  def edit(id: Long) = withAuth { username => implicit request => {
    val foundPosts = BlogPost.byId(id)
    if (!foundPosts.isEmpty) {
      val post = foundPosts.head
      val form = blogPostForm.fill(post)//blogPostForm.bind(map)
      Ok(views.html.addBlogPost(form))
    }
    else
      Redirect(routes.Blog.list(1))
    }
  }

  def update() = withAuth { username => implicit request =>
    blogPostForm.bindFromRequest.fold(
      errors => BadRequest(views.html.addBlogPost(errors)),
      tuple => {
        BlogPost.update(tuple)
        Redirect(routes.Blog.list(1))
      }
    )
  }

  def delete(id: String) = withAuth { username => implicit request =>
    BlogPost.delete(id);
    Redirect(routes.Blog.list(1))
  }

}