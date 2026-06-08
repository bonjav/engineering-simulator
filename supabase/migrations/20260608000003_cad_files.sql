-- Epic 003 — CAD File Upload
-- Adds RLS policies for cad_files and creates the Supabase Storage bucket.

-- ─── cad_files RLS ────────────────────────────────────────────────────────────
ALTER TABLE public.cad_files ENABLE ROW LEVEL SECURITY;

-- Any project member can view file metadata
CREATE POLICY "cad_files_member_select" ON public.cad_files
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id AND pm.user_id = auth.uid()
        )
    );

-- Any project member can upload (insert) a file record
CREATE POLICY "cad_files_member_insert" ON public.cad_files
    FOR INSERT
    WITH CHECK (
        uploaded_by = auth.uid()
        AND EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id AND pm.user_id = auth.uid()
        )
    );

-- Only the uploader or project owner can delete
CREATE POLICY "cad_files_uploader_or_owner_delete" ON public.cad_files
    FOR DELETE
    USING (
        uploaded_by = auth.uid()
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = project_id
              AND pm.user_id = auth.uid()
              AND pm.role = 'OWNER'
        )
    );

-- Service role bypass
CREATE POLICY "cad_files_service_role_all" ON public.cad_files
    USING (auth.role() = 'service_role');

-- ─── Supabase Storage bucket ──────────────────────────────────────────────────
-- Create a private bucket for CAD files.
-- Run this via the Supabase dashboard or CLI:
--
--   supabase storage create cad-files --private
--
-- Storage RLS is managed via Supabase dashboard policies.
-- Signed URLs (generated server-side with service_role key) are used for downloads.
--
-- Recommended Storage policies (apply via dashboard):
--
-- INSERT (upload): authenticated users who are project members
--   (auth.uid()::text = (storage.foldername(name))[1])  -- enforces project-scoped paths
--
-- SELECT (download): via signed URL only (bucket is private)
--
-- DELETE: service_role only (Spring Boot handles delete via service_role key)
