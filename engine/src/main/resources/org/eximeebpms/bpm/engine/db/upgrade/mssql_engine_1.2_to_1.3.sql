insert into ACT_GE_SCHEMA_LOG
values ('1400', CURRENT_TIMESTAMP, '1.3.0');
-- create business event outbox table --
create table ACT_RU_BUS_EVT_OBX (
    ID_               bigint IDENTITY(1,1) NOT NULL,
    CREATED_DATE_     datetime2 not null,
    BUSINESS_EVENT_   nvarchar(max) not null,
    EVENT_TYPE_       nvarchar(255),
    PROC_INST_ID_     nvarchar(64),
    ROOT_PROC_INST_ID_ nvarchar(64),
    PROC_DEF_KEY_     nvarchar(255),
    TASK_ID_          nvarchar(64),
    PROCESSED_        tinyint not null default 0,
    PROCESSED_DATE_   datetime2,
    primary key (ID_)
);
create index ACT_IDX_BEO_PROC_INST on ACT_RU_BUS_EVT_OBX(PROC_INST_ID_);
create index ACT_IDX_BEO_UNPROCESSED on ACT_RU_BUS_EVT_OBX(ID_) where PROCESSED_ = 0;

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
