# Blog schema

# --- !Ups

CREATE TABLE blog_post (
    id INT NOT NULL auto_increment,
    title varchar(511),
    summary varchar(2048),
    handler varchar(255) NOT NULL,
    content TEXT,
    published TIMESTAMP NOT NULL,
    CONSTRAINT blog_post_ID_pk PRIMARY KEY (id),
    INDEX blog_post_handler_idx (handler)
) ENGINE=INNODB;

CREATE TABLE blog_post_tag (
    blog_post_id INT NOT NULL,
    tag varchar(64),
    INDEX blog_post_tag_idx (tag),
    FOREIGN KEY (blog_post_id)
            REFERENCES blog_post(id)
            ON DELETE CASCADE
) ENGINE=INNODB;

# --- !Downs

DROP TABLE blog_post_tag;
DROP TABLE blog_post;