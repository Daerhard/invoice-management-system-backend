CREATE TABLE purchase_invoice
(
    id           BIGINT AUTO_INCREMENT,
    product_name VARCHAR(255)   NOT NULL,
    amount       INT            NOT NULL,
    price        DECIMAL(19, 4) NOT NULL,
    invoice_date DATE           NOT NULL,
    pdf_data     LONGBLOB       NOT NULL,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,
    PRIMARY KEY (id)
);
