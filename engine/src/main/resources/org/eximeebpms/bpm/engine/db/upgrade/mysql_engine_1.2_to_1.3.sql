insert into ACT_GE_SCHEMA_LOG
values ('1400', CURRENT_TIMESTAMP, '1.3.0');
-- create business event outbox table --
create table ACT_RU_BUS_EVT_OBX (
    ID_               bigint NOT NULL AUTO_INCREMENT,
    CREATED_DATE_     datetime(3) not null,
    BUSINESS_EVENT_   longtext not null,
    EVENT_TYPE_       varchar(255),
    PROC_INST_ID_     varchar(64),
    ROOT_PROC_INST_ID_ varchar(64),
    PROC_DEF_KEY_     varchar(255),
    TASK_ID_          varchar(64),
    PROCESSED_        tinyint(1) not null default 0,
    PROCESSED_DATE_   datetime(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;
create index ACT_IDX_BEO_PROC_INST on ACT_RU_BUS_EVT_OBX(PROC_INST_ID_);
create index ACT_IDX_BEO_UNPROCESSED on ACT_RU_BUS_EVT_OBX(PROCESSED_, ID_);

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
