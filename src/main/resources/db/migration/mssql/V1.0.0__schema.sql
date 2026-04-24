create table UserActivity
(
    activityTime datetime2(6),
    id           bigint identity,
    username     varchar(255) not null,
    primary key (id)
);

create table UserProfile
(
    id       bigint identity,
    fullName varchar(255) not null,
    username varchar(255) not null unique,
    primary key (id)
);

alter table UserActivity
    add constraint FKgw8obbdc2yw43lhaj8qequb4e
    foreign key (username)
    references UserProfile (username);
