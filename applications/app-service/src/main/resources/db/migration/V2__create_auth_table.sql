CREATE TABLE auths (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255)  NOT NULL UNIQUE,
    token      VARCHAR(3000) NOT NULL,
    expires_in INT           NOT NULL,
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP
);
