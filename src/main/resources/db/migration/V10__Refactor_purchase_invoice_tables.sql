-- Refactor purchase_invoice: remove per-item columns, add total_price
ALTER TABLE purchase_invoice
    DROP COLUMN amount,
    DROP COLUMN price,
    DROP COLUMN invoice_date,
    ADD COLUMN total_price DECIMAL(19, 4) NOT NULL DEFAULT 0.0000;

-- Create purchase_invoice_item table
CREATE TABLE IF NOT EXISTS purchase_invoice_item
(
    id                  INT            AUTO_INCREMENT,
    purchase_invoice_id INT            NOT NULL,
    purchase_type       VARCHAR(20)    NOT NULL,
    amount              INT            NOT NULL,
    price               DECIMAL(19, 4) NOT NULL,
    invoice_date        DATE           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_invoice_item_invoice FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoice (id) ON DELETE CASCADE
);

-- Re-link purchase_invoice_document from purchase_invoice to purchase_invoice_item
ALTER TABLE purchase_invoice_document
    DROP COLUMN purchase_invoice_id,
    ADD COLUMN purchase_invoice_item_id INT NULL,
    ADD CONSTRAINT fk_purchase_invoice_document_item FOREIGN KEY (purchase_invoice_item_id) REFERENCES purchase_invoice_item (id) ON DELETE CASCADE;
