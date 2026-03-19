CREATE TABLE IF NOT EXISTS invoice
(
    id          BIGINT AUTO_INCREMENT,
    order_id    INT      NOT NULL,
    created_at  DATETIME NOT NULL,
    invoice_pdf LONGBLOB NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_order_id (order_id),
    CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES cardmarket_order (id)
);
