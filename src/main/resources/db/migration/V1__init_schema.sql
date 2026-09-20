CREATE TABLE member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    created_at DATETIME     NOT NULL,
    CONSTRAINT uk_member_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE post (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    member_id  BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB;

CREATE INDEX idx_post_member_id ON post (member_id);
CREATE INDEX idx_post_created_at ON post (created_at);

CREATE TABLE comment (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    VARCHAR(1000) NOT NULL,
    post_id    BIGINT        NOT NULL,
    member_id  BIGINT        NOT NULL,
    parent_id  BIGINT        NULL,
    created_at DATETIME      NOT NULL,
    updated_at DATETIME      NOT NULL,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_comment_post_id ON comment (post_id);
CREATE INDEX idx_comment_member_id ON comment (member_id);
CREATE INDEX idx_comment_parent_id ON comment (parent_id);
