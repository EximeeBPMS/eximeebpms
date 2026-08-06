insert into ACT_GE_SCHEMA_LOG
values ('1400', CURRENT_TIMESTAMP, '1.3.0');

-- script guard violation log --
create table ACT_RU_SCRIPT_VIOLATION (
    ID_               nvarchar(64) not null,
    TIMESTAMP_        datetime2 not null,
    PROC_DEF_KEY_     nvarchar(255),
    ACTIVITY_ID_      nvarchar(255),
    LANGUAGE_         nvarchar(64),
    SOURCE_TYPE_      nvarchar(64),
    ORIGIN_           nvarchar(64),
    RULE_CODE_        nvarchar(255),
    REASON_           nvarchar(1000),
    primary key (ID_)
);
