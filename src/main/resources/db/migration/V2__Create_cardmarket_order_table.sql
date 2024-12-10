CREATE TABLE cardmarket_order
(
    id                INT AUTO_INCREMENT,
    customer_user_name VARCHAR(255) NOT NULL,
    external_order_id BIGINT       NOT NULL,
    date_of_payment   DATE     NOT NULL,
    article_count     INT          NOT NULL,
    merchandise_value DOUBLE       NOT NULL,
    shipment_cost     DOUBLE       NOT NULL,
    total_value       DOUBLE       NOT NULL,
    commission        DOUBLE       NOT NULL,
    currency          VARCHAR(255) NOT NULL DEFAULT 'EUR',
    PRIMARY KEY (id)
);