create table device_info
(
    id   bigint       not null auto_increment primary key,
    name varchar(255) not null unique,
    mac  varchar(255) not null
);

insert into device_info(name, mac)
values ('Air Pod 3', '00:00:00:00');
insert into device_info(name, mac)
values ('Air Pod Pro', '01:02:03:04');
