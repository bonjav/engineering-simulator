-- Epic 002 — Project Management
-- Adds RLS policies for projects and project_members.
-- Run via: supabase db push

-- ─── projects RLS ─────────────────────────────────────────────────────────────
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;

-- Any member (including owner) can view the project
CREATE POLICY "project_members_select" ON public.projects
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = id AND pm.user_id = auth.uid()
        )
    );

-- Only owners can insert
CREATE POLICY "project_owner_insert" ON public.projects
    FOR INSERT
    WITH CHECK (owner_id = auth.uid());

-- Owners and editors can update
CREATE POLICY "project_editor_update" ON public.projects
    FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = id
              AND pm.user_id = auth.uid()
              AND pm.role IN ('OWNER', 'EDITOR')
        )
    );

-- Only owners can delete
CREATE POLICY "project_owner_delete" ON public.projects
    FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = id
              AND pm.user_id = auth.uid()
              AND pm.role = 'OWNER'
        )
    );

-- Service role bypass
CREATE POLICY "projects_service_role_all" ON public.projects
    USING (auth.role() = 'service_role');

-- ─── project_members RLS ──────────────────────────────────────────────────────
ALTER TABLE public.project_members ENABLE ROW LEVEL SECURITY;

-- Any member can view other members of the same project
CREATE POLICY "members_select_same_project" ON public.project_members
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members me
            WHERE me.project_id = project_id AND me.user_id = auth.uid()
        )
    );

-- Only owners can add/remove members
CREATE POLICY "members_owner_insert" ON public.project_members
    FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id
              AND pm.user_id = auth.uid()
              AND pm.role = 'OWNER'
        )
    );

CREATE POLICY "members_owner_delete" ON public.project_members
    FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id
              AND pm.user_id = auth.uid()
              AND pm.role = 'OWNER'
        )
    );

-- Service role bypass
CREATE POLICY "members_service_role_all" ON public.project_members
    USING (auth.role() = 'service_role');

-- ─── updated_at trigger for projects ─────────────────────────────────────────
CREATE TRIGGER set_projects_updated_at
    BEFORE UPDATE ON public.projects
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();
