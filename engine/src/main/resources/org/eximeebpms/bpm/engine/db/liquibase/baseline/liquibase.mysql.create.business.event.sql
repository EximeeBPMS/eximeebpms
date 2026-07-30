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
