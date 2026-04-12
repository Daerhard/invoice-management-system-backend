-- Create refund table
CREATE TABLE IF NOT EXISTS refund
(
    id    INT            AUTO_INCREMENT,
    value DECIMAL(19, 4) NOT NULL,
    year  INT            NOT NULL,
    PRIMARY KEY (id)
);

-- Create supply table
CREATE TABLE IF NOT EXISTS supply
(
    id           INT            AUTO_INCREMENT,
    value        DECIMAL(19, 4) NOT NULL,
    supply_date  DATE           NOT NULL,
    product      VARCHAR(255)   NOT NULL,
    PRIMARY KEY (id)
);

-- Create supply_invoice_document table
CREATE TABLE IF NOT EXISTS supply_invoice_document
(
    id         INT  AUTO_INCREMENT,
    pdf_data   LONGBLOB NOT NULL,
    supply_id  INT  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_supply_invoice_document_supply FOREIGN KEY (supply_id) REFERENCES supply (id) ON DELETE CASCADE
);
