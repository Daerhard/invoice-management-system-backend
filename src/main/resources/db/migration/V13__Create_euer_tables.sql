-- Create euer_report table
CREATE TABLE IF NOT EXISTS euer_report
(
    id         BIGINT  NOT NULL AUTO_INCREMENT,
    year       INT     NOT NULL,
    published  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATE    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_euer_report_year (year)
);

-- Create euer_position table
CREATE TABLE IF NOT EXISTS euer_position
(
    id                       BIGINT         NOT NULL AUTO_INCREMENT,
    euer_report_id           BIGINT         NOT NULL,
    section                  VARCHAR(100)   NOT NULL,
    description              VARCHAR(500)   NOT NULL,
    value                    DECIMAL(19, 4) NOT NULL,
    automatically_calculated BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_euer_position_report FOREIGN KEY (euer_report_id) REFERENCES euer_report (id) ON DELETE CASCADE
);
