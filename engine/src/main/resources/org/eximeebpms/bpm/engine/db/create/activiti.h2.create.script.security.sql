-- script guard violation log --
create table ACT_RU_SCRIPT_VIOLATION (
    ID_               varchar(64) not null,
    TIMESTAMP_        timestamp not null,
    PROC_DEF_KEY_     varchar(255),
    ACTIVITY_ID_      varchar(255),
    LANGUAGE_         varchar(64),
    SOURCE_TYPE_      varchar(64),
    ORIGIN_           varchar(64),
    RULE_CODE_        varchar(255),
    REASON_           varchar(1000),
    primary key (ID_)
);
