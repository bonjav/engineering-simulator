-- Epic 001 — Authentication
-- Creates the user_profiles table managed by the Spring Boot service.
-- RLS policies and the auth.users FK are applied separately via Supabase migrations.

CREATE TABLE IF NOT EXISTS user_profiles (
    id         UUID         NOT NULL,
    email      TEXT         NOT NULL,
    full_name  VARCHAR(100),
    company    VARCHAR(100),
    role       VARCHAR(20)  NOT NULL DEFAULT 'user',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_profiles PRIMARY KEY (id),
    CONSTRAINT uq_user_profiles_email UNIQUE (email),
    CONSTRAINT chk_user_profiles_role CHECK (role IN ('user', 'admin'))
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_profiles (email);
