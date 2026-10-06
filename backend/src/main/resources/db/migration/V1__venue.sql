create table section (
    id bigint primary key,
    name varchar(64) not null,
    tier varchar(16) not null,
    base_price_cents bigint not null,
    row_count integer not null,
    seats_per_row integer not null
);

create table seat (
    id bigint primary key,
    section_id bigint not null references section (id),
    row_label varchar(4) not null,
    row_index integer not null,
    seat_number integer not null,
    x integer not null,
    y integer not null,
    status varchar(16) not null,
    held_by uuid,
    hold_expires_at timestamp(6) with time zone,
    held_price_cents bigint,
    version bigint not null default 0,
    seq bigint not null default 0
);

create index idx_seat_section on seat (section_id);
create index idx_seat_status_expiry on seat (status, hold_expires_at);
create index idx_seat_held_by on seat (held_by);
