insert into ACT_GE_SCHEMA_LOG
values ('1400', CURRENT_TIMESTAMP, '1.3.0');

-- script guard violation log --
create table ACT_RU_SCRIPT_VIOLATION (
    ID_               NVARCHAR2(64) not null,
    TIMESTAMP_        TIMESTAMP(6) not null,
    PROC_DEF_KEY_     NVARCHAR2(255),
    ACTIVITY_ID_      NVARCHAR2(255),
    LANGUAGE_         NVARCHAR2(64),
    SOURCE_TYPE_      NVARCHAR2(64),
    ORIGIN_           NVARCHAR2(64),
    RULE_CODE_        NVARCHAR2(255),
    REASON_           NVARCHAR2(1000),
    primary key (ID_)
);
