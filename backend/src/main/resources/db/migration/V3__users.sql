create table app_user (
    id uuid primary key,
    username varchar(32) not null unique,
    display_name varchar(64) not null,
    password_hash varchar(100) not null,
    created_at timestamp(6) with time zone not null
);
