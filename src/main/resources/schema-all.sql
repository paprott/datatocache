drop table PERSON IF EXISTS;

create table PERSON (
    ID bigint not null,
    EMAIL varchar(255),
    FIRST_NAME varchar(255),
    JOINED_DATE date,
    LAST_NAME varchar(255),
    primary key (ID));
