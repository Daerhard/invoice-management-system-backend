CREATE TABLE order_item
(
    id                  INT AUTO_INCREMENT,
    cardmarket_order_id INT          NOT NULL,
    count               INT          NOT NULL,
    `condition`         VARCHAR(255) NOT NULL,
    price               DOUBLE       NOT NULL,
    is_first_edition    BOOLEAN      NOT NULL,
    card_id_konami_set  VARCHAR(255) NOT NULL,
    card_id_number      INT          NOT NULL,
    PRIMARY KEY (id)
);
