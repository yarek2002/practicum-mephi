CREATE TABLE IF NOT EXISTS draws (
    id              BIGSERIAL PRIMARY KEY,
    status          VARCHAR(20)  NOT NULL,
    numbers_count   INTEGER      NOT NULL DEFAULT 6,
    max_number      INTEGER      NOT NULL DEFAULT 49,
    winning_numbers INTEGER[],
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS tickets (
    id          BIGSERIAL PRIMARY KEY,
    draw_id     BIGINT       NOT NULL REFERENCES draws(id),
    numbers     INTEGER[]    NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_draws_status ON draws(status);
CREATE INDEX IF NOT EXISTS idx_tickets_draw_id ON tickets(draw_id);
