CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT       NOT NULL,
    token      VARCHAR(200) NOT NULL,
    expires_at DATETIME     NOT NULL,
    created_at DATETIME     NOT NULL,
    CONSTRAINT uk_refresh_token_member_id UNIQUE (member_id),
    CONSTRAINT uk_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB;
