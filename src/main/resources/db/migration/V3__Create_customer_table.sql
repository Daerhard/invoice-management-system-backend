CREATE TABLE customer
(
    user_name       VARCHAR(255),
    full_name       VARCHAR(255) NOT NULL,
    street          VARCHAR(255) NOT NULL,
    city            VARCHAR(255) NOT NULL,
    country         VARCHAR(255) NOT NULL,
    is_professional BOOLEAN      NOT NULL,
    vat_number      VARCHAR(255) NULL,
    PRIMARY KEY (user_name)
);
