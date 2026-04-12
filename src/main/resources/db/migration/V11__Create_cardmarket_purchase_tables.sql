CREATE TABLE cardmarket_purchase
(
    id                 INT AUTO_INCREMENT,
    seller_user_name   VARCHAR(255) NOT NULL,
    external_order_id  BIGINT       NOT NULL,
    date_of_payment    DATE         NOT NULL,
    article_count      INT          NOT NULL,
    merchandise_value  DOUBLE       NOT NULL,
    shipment_cost      DOUBLE       NOT NULL,
    trustee_fee        DOUBLE       NOT NULL,
    total_value        DOUBLE       NOT NULL,
    currency           VARCHAR(255) NOT NULL DEFAULT 'EUR',
    PRIMARY KEY (id)
);

CREATE TABLE purchase_item
(
    id                      INT AUTO_INCREMENT,
    cardmarket_purchase_id  INT          NOT NULL,
    count                   INT          NOT NULL,
    `condition`             VARCHAR(255) NOT NULL,
    price                   DOUBLE       NOT NULL,
    is_first_edition        BOOLEAN      NOT NULL,
    card_id_konami_set      VARCHAR(255) NOT NULL,
    card_id_konami_number   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
