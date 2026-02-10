create table error_audit (
    id bigserial primary key,
    created_at timestamp not null,
    thread_name varchar(255),
    location varchar(512),
    exception_type varchar(512) not null,
    message varchar(2048)
);

create index idx_error_audit_created_at on error_audit (created_at desc);
create index idx_error_audit_exception_type on error_audit (exception_type);
