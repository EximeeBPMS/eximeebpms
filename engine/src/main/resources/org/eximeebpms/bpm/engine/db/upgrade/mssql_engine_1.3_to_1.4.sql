insert into ACT_GE_SCHEMA_LOG
values ('1500', CURRENT_TIMESTAMP, '1.4.0');
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

-- remove CMMN support --

-- drop foreign key constraints referencing CMMN tables --
alter table ACT_RU_VARIABLE drop constraint ACT_FK_VAR_CASE_EXE;
alter table ACT_RU_VARIABLE drop constraint ACT_FK_VAR_CASE_INST;
alter table ACT_RU_TASK drop constraint ACT_FK_TASK_CASE_EXE;
alter table ACT_RU_TASK drop constraint ACT_FK_TASK_CASE_DEF;

-- drop indexes on CMMN columns in non-CMMN tables --
drop index ACT_IDX_TASK_CASE_EXEC on ACT_RU_TASK;
drop index ACT_IDX_TASK_CASE_DEF_ID on ACT_RU_TASK;
drop index ACT_IDX_VARIABLE_CASE_EXEC on ACT_RU_VARIABLE;
drop index ACT_IDX_VARIABLE_CASE_INST on ACT_RU_VARIABLE;
drop index ACT_IDX_HI_DETAIL_CASE_INST on ACT_HI_DETAIL;
drop index ACT_IDX_HI_DETAIL_CASE_EXEC on ACT_HI_DETAIL;
drop index ACT_IDX_HI_CASEVAR_CASE_INST on ACT_HI_VARINST;
drop index ACT_IDX_HI_DEC_INST_CI on ACT_HI_DECINST;

-- drop CMMN runtime tables --
drop table ACT_RU_CASE_SENTRY_PART;
drop table ACT_RU_CASE_EXECUTION;
drop table ACT_RE_CASE_DEF;

-- drop CMMN history tables --
drop table ACT_HI_CASEACTINST;
drop table ACT_HI_CASEINST;

-- drop CMMN columns from ACT_RU_EXECUTION --
alter table ACT_RU_EXECUTION drop column SUPER_CASE_EXEC_;
alter table ACT_RU_EXECUTION drop column CASE_INST_ID_;

-- drop CMMN columns from ACT_RU_TASK --
alter table ACT_RU_TASK drop column CASE_EXECUTION_ID_;
alter table ACT_RU_TASK drop column CASE_INST_ID_;
alter table ACT_RU_TASK drop column CASE_DEF_ID_;

-- drop CMMN columns from ACT_RU_VARIABLE --
alter table ACT_RU_VARIABLE drop column CASE_EXECUTION_ID_;
alter table ACT_RU_VARIABLE drop column CASE_INST_ID_;

-- drop CMMN columns from ACT_HI_PROCINST --
alter table ACT_HI_PROCINST drop column SUPER_CASE_INSTANCE_ID_;
alter table ACT_HI_PROCINST drop column CASE_INST_ID_;

-- drop CMMN columns from ACT_HI_ACTINST --
alter table ACT_HI_ACTINST drop column CALL_CASE_INST_ID_;

-- drop CMMN columns from ACT_HI_TASKINST --
alter table ACT_HI_TASKINST drop column CASE_DEF_KEY_;
alter table ACT_HI_TASKINST drop column CASE_DEF_ID_;
alter table ACT_HI_TASKINST drop column CASE_INST_ID_;
alter table ACT_HI_TASKINST drop column CASE_EXECUTION_ID_;

-- drop CMMN columns from ACT_HI_VARINST --
alter table ACT_HI_VARINST drop column CASE_DEF_KEY_;
alter table ACT_HI_VARINST drop column CASE_DEF_ID_;
alter table ACT_HI_VARINST drop column CASE_INST_ID_;
alter table ACT_HI_VARINST drop column CASE_EXECUTION_ID_;

-- drop CMMN columns from ACT_HI_DETAIL --
alter table ACT_HI_DETAIL drop column CASE_DEF_KEY_;
alter table ACT_HI_DETAIL drop column CASE_DEF_ID_;
alter table ACT_HI_DETAIL drop column CASE_INST_ID_;
alter table ACT_HI_DETAIL drop column CASE_EXECUTION_ID_;

-- drop CMMN columns from ACT_HI_OP_LOG --
alter table ACT_HI_OP_LOG drop column CASE_DEF_ID_;
alter table ACT_HI_OP_LOG drop column CASE_INST_ID_;
alter table ACT_HI_OP_LOG drop column CASE_EXECUTION_ID_;

-- drop CMMN columns from ACT_HI_DECINST --
alter table ACT_HI_DECINST drop column CASE_DEF_KEY_;
alter table ACT_HI_DECINST drop column CASE_DEF_ID_;
alter table ACT_HI_DECINST drop column CASE_INST_ID_;

-- replace deprecated MSSQL `image` type with `varbinary(max)` --
alter table ACT_ID_INFO alter column PASSWORD_ varbinary(max);
alter table ACT_GE_BYTEARRAY alter column BYTES_ varbinary(max);
alter table ACT_HI_COMMENT alter column FULL_MSG_ varbinary(max);
