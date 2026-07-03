insert into ACT_GE_SCHEMA_LOG
values ('1400', CURRENT_TIMESTAMP, '1.3.0');

-- script guard violation log --
create table ACT_RU_SCRIPT_VIOLATION (
    ID_               varchar(64) not null,
    TIMESTAMP_        timestamp(3) not null,
    PROC_DEF_KEY_     varchar(255),
    ACTIVITY_ID_      varchar(255),
    LANGUAGE_         varchar(64),
    SOURCE_TYPE_      varchar(64),
    ORIGIN_           varchar(64),
    RULE_CODE_        varchar(255),
    REASON_           varchar(1000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_bin;
