-- The sent_at column is changed from DATETIME to DATE.
-- Any existing DATETIME values will have their time component truncated to midnight.
ALTER TABLE invoice
    MODIFY COLUMN sent_at DATE NULL;
