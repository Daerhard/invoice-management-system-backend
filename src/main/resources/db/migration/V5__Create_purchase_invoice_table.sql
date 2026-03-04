CREATE TABLE IF NOT EXISTS purchase_invoice
(
    id           Int            AUTO_INCREMENT,
    product_name VARCHAR(255)   NOT NULL,
    amount       INT            NOT NULL,
    price        DOUBLE         NOT NULL,
    invoice_date DATE           NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS purchase_invoice_document
(
    id                   Int        AUTO_INCREMENT,
    purchase_invoice_id  Int        NOT NULL,
    pdf_data             LONGBLOB   NOT NULL,
    PRIMARY KEY (id)
);