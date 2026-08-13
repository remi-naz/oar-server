-- Baseline: captures the schema as it exists today (created by Hibernate ddl-auto).
-- On environments that already have this schema (e.g. the current Supabase instance),
-- Flyway is configured to baseline at this version rather than re-run it.
-- On a fresh database, this script builds the schema from scratch.

create table users
(
    id            uuid primary key,
    google_subject text unique,
    email         text unique,
    display_name  text        not null,
    photo_url     text,
    created_at    timestamptz not null,
    updated_at    timestamptz not null,
    deleted_at    timestamptz
);

create table sessions
(
    id                          uuid primary key,
    user_id                     uuid        not null references users (id) on delete cascade,
    refresh_token_hash          text        not null unique,
    previous_refresh_token_hash text unique,
    device_label                text,
    created_at                  timestamptz not null,
    expires_at                  timestamptz not null,
    last_used_at                timestamptz,
    revoked_at                  timestamptz
);

create table user_key_wraps
(
    id            uuid primary key,
    user_id       uuid        not null references users (id),
    key_version   integer     not null,
    wrap_method   text        not null,
    wrapped_key   bytea       not null,
    wrap_params   jsonb       not null,
    created_at    timestamptz not null,
    last_used_at  timestamptz,
    unique (user_id, key_version, wrap_method)
);
