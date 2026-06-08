-- Epic 003 — CAD File Upload
-- Creates the cad_files metadata table.
-- Supabase Storage holds the actual file bytes; this table is the metadata index.

CREATE TABLE IF NOT EXISTS cad_files (
    id                UUID         NOT NULL,
    project_id        UUID         NOT NULL,
    uploaded_by       UUID         NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    file_size_bytes   BIGINT       NOT NULL,
    storage_path      VARCHAR(500) NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'UPLOADING',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cad_files             PRIMARY KEY (id),
    CONSTRAINT fk_cad_files_project     FOREIGN KEY (project_id)  REFERENCES projects (id)      ON DELETE CASCADE,
    CONSTRAINT fk_cad_files_uploader    FOREIGN KEY (uploaded_by) REFERENCES user_profiles (id) ON DELETE CASCADE,
    CONSTRAINT chk_cad_files_status     CHECK (status IN ('UPLOADING', 'READY', 'FAILED')),
    CONSTRAINT chk_cad_files_size       CHECK (file_size_bytes > 0)
);

CREATE INDEX IF NOT EXISTS idx_cad_files_project_id  ON cad_files (project_id);
CREATE INDEX IF NOT EXISTS idx_cad_files_uploaded_by ON cad_files (uploaded_by);
CREATE INDEX IF NOT EXISTS idx_cad_files_status      ON cad_files (status);
