-- Epic 002 — Project Management
-- Creates projects and project_members tables.
-- RLS policies are applied separately via Supabase migrations.

CREATE TABLE IF NOT EXISTS projects (
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    owner_id    UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES user_profiles (id) ON DELETE CASCADE,
    CONSTRAINT chk_projects_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS project_members (
    id          BIGSERIAL    NOT NULL,
    project_id  UUID         NOT NULL,
    user_id     UUID         NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
    added_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_project_members PRIMARY KEY (id),
    CONSTRAINT uq_project_members_project_user UNIQUE (project_id, user_id),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES user_profiles (id) ON DELETE CASCADE,
    CONSTRAINT chk_project_members_role CHECK (role IN ('VIEWER', 'EDITOR', 'OWNER'))
);

CREATE INDEX IF NOT EXISTS idx_projects_owner_id    ON projects (owner_id);
CREATE INDEX IF NOT EXISTS idx_projects_status      ON projects (status);
CREATE INDEX IF NOT EXISTS idx_project_members_user ON project_members (user_id);
