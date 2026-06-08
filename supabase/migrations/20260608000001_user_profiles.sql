-- Epic 001 — Authentication
-- Adds auth.users FK, RLS policies, and an auto-provisioning trigger.
-- Run this migration via the Supabase CLI (supabase db push).

-- ─── Foreign Key to auth.users ────────────────────────────────────────────────
ALTER TABLE public.user_profiles
    ADD CONSTRAINT fk_user_profiles_auth_users
    FOREIGN KEY (id) REFERENCES auth.users (id) ON DELETE CASCADE;

-- ─── Row-Level Security ────────────────────────────────────────────────────────
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;

-- Authenticated users can read and update only their own row.
CREATE POLICY "users_select_own" ON public.user_profiles
    FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "users_update_own" ON public.user_profiles
    FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- Spring Boot connects with the service_role key and bypasses RLS by default.
-- This policy is a safety net for any future direct Supabase client access.
CREATE POLICY "service_role_all" ON public.user_profiles
    USING (auth.role() = 'service_role');

-- ─── Auto-provisioning trigger ────────────────────────────────────────────────
-- Creates a user_profiles row automatically when a user signs up via Supabase Auth.
CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.user_profiles (id, email)
    VALUES (NEW.id, NEW.email)
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_auth_user();

-- ─── updated_at auto-bump ─────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER set_user_profiles_updated_at
    BEFORE UPDATE ON public.user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();
