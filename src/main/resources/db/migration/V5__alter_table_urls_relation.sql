ALTER TABLE urls
    ADD COLUMN user_id UUID NOT NULL;

ALTER TABLE urls
    ADD CONSTRAINT fk_urls_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_urls_user_id ON urls(user_id);