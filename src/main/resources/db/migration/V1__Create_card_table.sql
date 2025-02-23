CREATE TABLE card
(
    konami_set           VARCHAR(255) NOT NULL,
    konami_number        VARCHAR(255) NOT NULL,
    complete_description TEXT         NOT NULL,
    product_name         VARCHAR(255) NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    language             VARCHAR(255) NOT NULL,
    rarity               VARCHAR(255) NOT NULL,
    product_id           BIGINT       NOT NULL,
    PRIMARY KEY (konami_set, konami_number)
);
