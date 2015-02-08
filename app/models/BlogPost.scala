package models

// Required for Anorm mapping capability

import java.sql.{Timestamp, Date}

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime

// Required for the Play db functionality
import play.api.db._
import play.api.Play.current


/**
 * DTO object carrying over blog post information
 *  handle is human readable part of url, no spaces
 *  title is a header of a blog post
 *  content is an html (?) formatted content
 *  published date is a dat when particular post has been published, used to sort posts by time
 */
case class BlogPost(id: Long, handle: String, title: String, summary: String, content: String, published: DateTime)

/**
 * Created by nickvoronin on 1/28/15.
 * A repository object allowing access to Blog Posts in DB
 */
object BlogPost {

  // Parser for mapping JDBC ResultSet to a single entity of BlogPost model
  val blogPost = {
    get[Long]("id") ~
    get[String]("handler") ~
    get[String]("title") ~
    get[String]("summary") ~
    get[String]("content") ~
    get[DateTime]("published") map {
      case id~handler~title~summary~content~published => BlogPost(id, handler, title, summary, content, published)
    }
  }

  /**
   * returns all blog posts. Should be retired.
   */
  @deprecated
  def all(): List[BlogPost] = DB.withConnection { implicit c =>
    SQL("select * from blog_post order by published desc").as(blogPost *)
  }

  /**
   * Returns blog posts with matching ids
   * @param ids ids to query
   * @return
   */
  @deprecated
  def all(ids: Seq[Int]): List[BlogPost] = DB.withConnection { implicit c =>
    SQL("select * from blog_post where id in ({ids}) order by published desc").on('ids -> ids).as(blogPost *)
  }

  /**
   * Returns blog posts for specified tag
   * @param tag
   * @return
   */
  def byTag(tag: String): List[BlogPost] = DB.withConnection { implicit c =>
    SQL("select * from blog_post where id in (select blog_post_id from blog_post_tag where tag = {tag}) order by published desc").on('tag -> tag).as(blogPost *)
  }

  /**
   * Creates a blog posts with specified fields
   * @param handler
   * @param title
   * @param content
   * @param tags
   * @return
   */
  def create(handler: String, title: String, summary: String, content: String, tags: Seq[String]) =
    DB.withConnection { implicit c => {
        val ids:Seq[Int] = SQL("INSERT INTO blog_post (id,title,summary,handler,content,published) VALUES (NULL, {title}, {summary}, {handler}, {content}, CURRENT_TIMESTAMP);").on(
          'title -> title,
          'summary -> summary,
          'handler -> handler,
          'content -> content
        ).executeInsert({get[Int]("GENERATED_KEY") map { GENERATED_KEY => GENERATED_KEY }} *)

        val sql = SQL("INSERT INTO blog_post_tag (blog_post_id, tag) values({id}, {tag})")
        val batchInsert = (sql.asBatch /: tags)(
          (sql, t) => ( sql.addBatchParams(ids.head, t) )
        )
        batchInsert.execute()
      }
    }

  /**
   * Deletes a blog post with specified id
   * @param id
   * @return
   */
  def delete(id: String) = DB.withConnection { implicit c =>
    SQL("delete from blog_post where id = {id}").on(
      'id -> id.toInt
    ).executeUpdate()
  }

  /**
   * Returns blog posts for specified page of a given size
   * @param pageNum starts with 0
   * @param pageSize size of a page to fetch
   * @return
   */
  def byPage(pageNum: Int, pageSize: Int) =
  if (pageNum < 0 || pageSize <= 0 || pageSize > 20)
    Nil
  else
    DB.withConnection { implicit c =>
      SQL("select * from blog_post order by published desc limit {skipRecords}, {size}")
        .on('size -> pageSize, 'skipRecords -> (pageSize * pageNum)).as(blogPost *)
    }

  def totalBlogPosts:Int = DB.withConnection { implicit c =>
    SQL("select count(id) TOTAL from blog_post").as({get[Int]("TOTAL") map { total => total }} *).head
  }

  def byHandle(handle:String):List[BlogPost] =
   if(handle.isEmpty)
     Nil
   else
     DB.withConnection { implicit c =>
       SQL("select * from blog_post where handler={handler} LIMIT 1")
         .on('handler -> handle).as(blogPost *)
     }
}
