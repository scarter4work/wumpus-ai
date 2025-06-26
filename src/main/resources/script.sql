create table if not exists databasechangelog
(
    id            varchar(255) not null,
    author        varchar(255) not null,
    filename      varchar(255) not null,
    dateexecuted  timestamp    not null,
    orderexecuted integer      not null,
    exectype      varchar(10)  not null,
    md5sum        varchar(35),
    description   varchar(255),
    comments      varchar(255),
    tag           varchar(255),
    liquibase     varchar(20),
    contexts      varchar(255),
    labels        varchar(255),
    deployment_id varchar(10)
);

alter table databasechangelog
    owner to postgres;

create table if not exists databasechangeloglock
(
    id          integer not null
        primary key,
    locked      boolean not null,
    lockgranted timestamp,
    lockedby    varchar(255)
);

alter table databasechangeloglock
    owner to postgres;

create table if not exists games
(
    id               uuid         not null
        primary key,
    player_name      varchar(255) not null,
    start_time       timestamp    not null,
    end_time         timestamp,
    status           varchar(50)  not null,
    arrows_remaining integer      not null,
    current_room_id  uuid         not null
);

alter table games
    owner to postgres;

create table if not exists rooms
(
    id            uuid                  not null
        primary key,
    north_room_id uuid,
    east_room_id  uuid,
    south_room_id uuid,
    west_room_id  uuid,
    has_wumpus    boolean default false not null,
    has_pit       boolean default false not null,
    has_bats      boolean default false not null
);

alter table rooms
    owner to postgres;

create table if not exists game_rooms
(
    id      uuid not null
        primary key,
    game_id uuid not null
        constraint fk_game_rooms_game_id
            references games,
    room_id uuid not null
        constraint fk_game_rooms_room_id
            references rooms
);

alter table game_rooms
    owner to postgres;

create table if not exists game_visited_rooms
(
    id         uuid      not null
        primary key,
    game_id    uuid      not null
        constraint fk_game_visited_rooms_game_id
            references games
            on delete cascade,
    room_id    uuid      not null
        constraint fk_game_visited_rooms_room_id
            references rooms
            on delete cascade,
    visited_at timestamp not null,
    constraint uk_game_visited_rooms_game_room
        unique (game_id, room_id)
);

alter table game_visited_rooms
    owner to postgres;


