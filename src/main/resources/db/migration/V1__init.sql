create table roles (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table users (
    id bigserial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);

create table user_roles (
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id),
    constraint fk_user_roles_user
        foreign key (user_id) references users (id),
    constraint fk_user_roles_role
        foreign key (role_id) references roles (id)
);

create table drivers (
    id bigserial primary key,
    name varchar(255) not null,
    experience integer not null,
    is_available boolean not null,
    earnings double precision not null
);

create table cars (
    id bigserial primary key,
    capacity double precision not null,
    is_broken boolean not null,
    version integer
);

create table orders (
    id bigserial primary key,
    destination varchar(255) not null,
    cargo_type varchar(255) not null,
    weight double precision not null,
    created_at timestamp not null
);

create table trips (
    id bigserial primary key,
    order_id bigint not null,
    driver_id bigint not null,
    car_id bigint not null,
    start_time timestamp not null,
    end_time timestamp,
    status varchar(255) not null,
    payment double precision,
    car_status_after_trip varchar(255),
    constraint fk_trips_order
        foreign key (order_id) references orders (id),
    constraint fk_trips_driver
        foreign key (driver_id) references drivers (id),
    constraint fk_trips_car
        foreign key (car_id) references cars (id)
);
