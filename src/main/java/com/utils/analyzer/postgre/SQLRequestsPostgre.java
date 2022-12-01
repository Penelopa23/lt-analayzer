package com.utils.analyzer.postgre;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SQLRequestsPostgre {

    REQUEST("SELECT\n" +
            "to_char(cast(t1.CREATION_TIME as date, ''YYYY-MM-DD HH24'') as TIME, \n" +
            "count(distinct(t1.OPERATION_ID)) as REQUEST\n" +
            "from {0} t1\n" +
            "and t1.creation_time between to_date(''{4}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{5}'', ''YYYY-MM-DD HH24:MI:SS'')\n" +
            "group_by to_char(cast(t1.CREATION_TIME as date), ''YYYY-MM-DD HH24''\n" +
            "ORDER BY TIME"),
    RESPONSE("SELECT\n" +
            "to_char(cast(t1.CREATION_TIME as date, ''YYYY-MM-DD HH24'') as TIME, \n" +
            "count(distinct(t2.OPERATION_ID)) as RESPONSE\n" +
            "from {0} t1\n" +
            "LEFT_JOIN\n" +
            "(SELECT OPERATION_ID, CREATION_TIME FROM {0} WHERE TYPE = ''{3}'') t2 ON t1.OPERATION_ID = t2.OPERATION_ID\n" +
            "where t1.type = ''{1}''\n" +
            "and t1.creation_time between to_date(''{4}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{5}'', ''YYYY-MM-DD HH24:MI:SS'')\n" +
            "group_by to_char(cast(t1.CREATION_TIME as date), ''YYYY-MM-DD HH24''\n" +
            "ORDER BY TIME"),
    SUCCESS("SELECT\n" +
            "to_char(cast(t1.CREATION_TIME as date, ''YYYY-MM-DD HH24'') as TIME, \n" +
            "count(distinct(t2.OPERATION_ID)) as SUCCESS\n" +
            "from {0} t1\n" +
            "LEFT_JOIN\n" +
            "(SELECT OPERATION_ID, CREATION_TIME FROM {0} WHERE TYPE = ''{2}'' OR TYPE = ''{6}'') t3 ON t1.OPERATION_ID = t3.OPERATION_ID\n" +
            "where t1.type = ''{1}''\n" +
            "and t1.creation_time between to_date(''{4}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{5}'', ''YYYY-MM-DD HH24:MI:SS'')\n" +
            "group_by to_char(cast(t1.CREATION_TIME as date), ''YYYY-MM-DD HH24''\n" +
            "ORDER BY TIME"),
    WORKFLOWBROKEN("SELECT\n" +
            "to_char(cast(t1.CREATION_TIME as date, ''YYYY-MM-DD HH24'') as TIME, \n" +
            "count(distinct(t2.OPERATION_ID)) as BROKEN\n" +
            "from {0} t1\n" +
            "LEFT_JOIN\n" +
            "(SELECT OPERATION_ID, CREATION_TIME FROM {0} WHERE TYPE = ''WORKFLOW_BROKEN'') t4 ON t1.OPERATION_ID = t4.OPERATION_ID\n" +
            "where t1.type = ''{1}''\n" +
            "and t1.creation_time between to_date(''{4}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{5}'', ''YYYY-MM-DD HH24:MI:SS'')\n" +
            "group_by to_char(cast(t1.CREATION_TIME as date), ''YYYY-MM-DD HH24''\n" +
            "ORDER BY TIME"),
    ERROR("SELECT\n" +
           "to_char(cast(t1.CREATION_TIME as date, ''YYYY-MM-DD HH24'') as TIME, \n" +
           "count(distinct(t2.OPERATION_ID)) as ERROR\n" +
           "from {0} t1\n" +
           "LEFT_JOIN\n" +
           "(SELECT OPERATION_ID, CREATION_TIME FROM {0} WHERE TYPE LIKE = ''%ERROR%'') t5 ON t1.OPERATION_ID = t5.OPERATION_ID\n" +
           "where t1.type = ''{1}''\n" +
           "and t1.creation_time between to_date(''{4}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{5}'', ''YYYY-MM-DD HH24:MI:SS'')\n" +
           "group_by to_char(cast(t1.CREATION_TIME as date), ''YYYY-MM-DD HH24''\n" +
           "ORDER BY TIME"),
    WORKFLOWBROKENWITHTEXT("SELECT\n" +
           "count(tt.OPERATION_ID) as OPERATION_ID, MIN(tt.START_TIME) as START_TIME, MAX(tt.FINISH_TIME) as FINISH_TIME, tt.ERROR FROM \n" +
           "(select t.operation_id, t.start_time, t.finish_time,\n" +
           "case\n" +
           "WHEN INSTR(t.error, ''SQLException'')>0 THEN replace(t.ERROR, substr(t.error, INSTR(t.error, ''SQLException'')))\n" +
           "WHEN INSTR(t.error, ''OperationID'')>0 THEN replace(t.ERROR, substr(t.error, INSTR(t.error, ''OperationID'')))\n" +
           "WHEN INSTR(t.error, ''message.id'')>0 THEN replace(t.ERROR, substr(t.error, INSTR(t.error, ''Message.id'')))\n" +
           "ELSE t.error\n" +
           "END as ERROR\n" +
           "from (\n" +
           "Select\n" +
           "(t1.OPERATION_ID) as OPERATION_ID,\n" +
           "(t1.CREATION_TIME) as START_TIME,\n" +
           "(t2.CREATION_TIME) as FINISH_TIME,\n" +
           "dbms_lob.substr(NVL(t2.headers, t2.data), 300) as ERROR\n" +
           "FROM\n" +
           "(SELECT OPERATION_ID, CREATION_TIME FROM {0} WHERE TYPE = ''{3}'') t1 LEFT JOIN\n" +
           "(SELECT OPERATION_ID, CREATION_TIME, HEADERS, DATA FROM {0} WHERE TYPE = ''WORKFLOW_BROKEN'') t2 ON t1.OPERATION_ID = t2.OPERATION_ID\n" +
           "where t2.CREATION_TIME between to_date(''{1}'', ''YYYY-MM-DD HH24:MI:SS'') and to_date(''{2}'', ''YYYY-MM-DD HH24:MI:SS'')) t ) tt\n" +
           "group_by tt.error\n"
    );

    private final String request;
}
