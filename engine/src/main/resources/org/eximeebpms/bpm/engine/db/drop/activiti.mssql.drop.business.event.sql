if exists (select name from sys.indexes where name = 'ACT_IDX_BEO_PROC_INST' and object_id = object_id('ACT_RU_BUS_EVT_OBX')) drop index ACT_RU_BUS_EVT_OBX.ACT_IDX_BEO_PROC_INST;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_BUS_EVT_OBX') drop table ACT_RU_BUS_EVT_OBX;
