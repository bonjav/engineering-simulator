-- Epic 004 — Simulation Workflow
-- RLS policies for simulation_runs.

ALTER TABLE public.simulation_runs ENABLE ROW LEVEL SECURITY;

-- Any project member can view simulation runs
CREATE POLICY "sim_runs_member_select" ON public.simulation_runs
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id AND pm.user_id = auth.uid()
        )
    );

-- Any project member can create a run
CREATE POLICY "sim_runs_member_insert" ON public.simulation_runs
    FOR INSERT
    WITH CHECK (
        requested_by = auth.uid()
        AND EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id AND pm.user_id = auth.uid()
        )
    );

-- Spring Boot (service_role) updates status, report, and error fields
CREATE POLICY "sim_runs_service_role_all" ON public.simulation_runs
    USING (auth.role() = 'service_role');
