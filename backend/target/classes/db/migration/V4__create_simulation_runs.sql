-- Epic 004 — Simulation Workflow
-- Stores simulation run state and the generated report (as JSON text).

CREATE TABLE IF NOT EXISTS simulation_runs (
    id                    UUID         NOT NULL,
    project_id            UUID         NOT NULL,
    cad_file_id           UUID         NOT NULL,
    requested_by          UUID         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    temporal_workflow_id  VARCHAR(255),
    report                TEXT,
    error_message         TEXT,
    started_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at          TIMESTAMPTZ,
    CONSTRAINT pk_simulation_runs          PRIMARY KEY (id),
    CONSTRAINT fk_sim_runs_project         FOREIGN KEY (project_id)  REFERENCES projects (id)      ON DELETE CASCADE,
    CONSTRAINT fk_sim_runs_cad_file        FOREIGN KEY (cad_file_id) REFERENCES cad_files (id)     ON DELETE CASCADE,
    CONSTRAINT fk_sim_runs_requester       FOREIGN KEY (requested_by) REFERENCES user_profiles (id) ON DELETE CASCADE,
    CONSTRAINT chk_sim_runs_status         CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_sim_runs_project_id  ON simulation_runs (project_id);
CREATE INDEX IF NOT EXISTS idx_sim_runs_status      ON simulation_runs (status);
CREATE INDEX IF NOT EXISTS idx_sim_runs_workflow_id ON simulation_runs (temporal_workflow_id);
