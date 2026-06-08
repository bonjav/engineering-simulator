package com.datacenterflow.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
    String url,
    String anonKey,
    String serviceRoleKey,
    String jwtSecret
) {}
