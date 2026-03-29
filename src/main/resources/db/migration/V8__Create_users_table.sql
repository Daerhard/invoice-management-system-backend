CREATE TABLE IF NOT EXISTS users
(
    id         INT          AUTO_INCREMENT,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    zip_code   VARCHAR(255) NOT NULL,
    city       VARCHAR(255) NOT NULL,
    street     VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);
