-- disk stores
CREATE DISKSTORE CONTEXTDISKSTORE;

-- tables
CREATE TABLE CONTEXT_HISTORY (EQUIP_ID VARCHAR(40) NOT NULL, CONTEXT_ID INTEGER NOT NULL, STOP_DATE TIMESTAMP NOT NULL, START_DATE TIMESTAMP, CONSTRAINT CONTEST_HISTORY_PK PRIMARY KEY(EQUIP_ID,CONTEXT_ID,STOP_DATE)) PARTITION BY COLUMN (EQUIP_ID,CONTEXT_ID,STOP_DATE) REDUNDANCY 1 PERSISTENT 'CONTEXTDISKSTORE';

CREATE TABLE CHART_HISTORY ( EQUIP_ID VARCHAR(40) NOT NULL, CONTEXT_ID INTEGER NOT NULL, STOP_DATE TIMESTAMP NOT NULL, SVID_NAME VARCHAR(64) NOT NULL, CONSTRAINT CHART_HISTORY_PK PRIMARY KEY(EQUIP_ID,CONTEXT_ID,STOP_DATE,SVID_NAME) )PARTITION BY COLUMN (EQUIP_ID,CONTEXT_ID,STOP_DATE) COLOCATE WITH (CONTEXT_HISTORY) REDUNDANCY 1 PERSISTENT 'CONTEXTDISKSTORE';

CREATE TABLE RECEIVED_HISTORY_LOG ( EQUIP_ID VARCHAR(40) NOT NULL, CONTEXT_ID INTEGER NOT NULL, STOP_DATE TIMESTAMP NOT NULL, UPDATE_DATE TIMESTAMP NOT NULL, EXEC_TIME DOUBLE, CONSTRAINT RECEIVER_HISTORY_LOG_PK PRIMARY KEY(EQUIP_ID,CONTEXT_ID,STOP_DATE,UPDATE_DATE) )PARTITION BY COLUMN (EQUIP_ID,CONTEXT_ID,STOP_DATE,UPDATE_DATE) REDUNDANCY 1 PERSISTENT 'CONTEXTDISKSTORE';

-- Index :
create index IDX01_CONTEXT_HISTORY on CONTEXT_HISTORY (CONTEXT_ID, EQUIP_ID);
create index IDX02_DISKSTOR_HISTORY on CONTEXT_HISTORY (EQUIP_ID, STOP_DATE);
