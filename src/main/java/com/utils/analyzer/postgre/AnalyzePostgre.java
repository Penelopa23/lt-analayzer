package com.utils.analyzer.postgre;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.utils.analyzer.utils.Connections;
import com.utils.analyzer.reportUtils.HeaderType;
import com.utils.analyzer.reportUtils.TimedReport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class AnalyzePostgre {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private Gson gson = new Gson();
    private Date from;
    private Date to;
    private PostgreModules modules;
    private Connection connection;

    @PostConstruct
    private void init() {
        connection = Connections.POSTGRE.DB();
    }

    private List<String> getSqlR() {
        List<String> sqlR = Arrays.asList(
                SQLRequestsPostgre.REQUEST.getRequest(),
                SQLRequestsPostgre.RESPONSE.getRequest(),
                SQLRequestsPostgre.SUCCESS.getRequest(),
                SQLRequestsPostgre.WORKFLOWBROKEN.getRequest(),
                SQLRequestsPostgre.ERROR.getRequest()
        );
        return sqlR;
    }

    @SneakyThrows
    private TimedReport executeRequest(List<String> sqlR) {
        HashMap<String, List> interResult = new HashMap<>();
        interResult.put("result", new ArrayList<Integer>());
        interResult.put("request", new ArrayList<Integer>());
        interResult.put("response", new ArrayList<Integer>());
        interResult.put("success", new ArrayList<Integer>());
        interResult.put("workflowBroken", new ArrayList<Integer>());
        interResult.put("error", new ArrayList<Integer>());
        interResult.put("sendError", new ArrayList<Integer>());
        interResult.put("startTimes", new ArrayList<Integer>());
        String sql = "";
        for(String s : sqlR) {
            sql = MessageFormat.format(
                    s,
                    modules.getTablePrefix(),
                    modules.getType().getReceiveRequests(),
                    modules.getType().getSuccess(),
                    modules.getType().getSendResponse(),
                    sdf.format(from),
                    sdf.format(to),
                    modules.getType().getOptional()
            );
            try (CallableStatement statement1 = connection.prepareCall(sql)) {
                try (ResultSet resultSet = statement1.executeQuery()){
                    int result = 0;
                    while(resultSet.next()) {
                        interResult.get("startTimes").add(resultSet.getInt("TIME"));
                        result += resultSet.getInt(2);
                        switch (resultSet.getMetaData().getColumnName(2)) {
                            case("request"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            case("response"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            case("success"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            case("workflowBroken"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            case("sendError"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            case("error"): interResult.get("request").add(resultSet.getInt("REQUEST")); break;
                            default: break;
                        }
                    }
                    interResult.get("result").add(result);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        TimedReport timedReport = setTimedReport(interResult);
        return timedReport;
    }

    @SneakyThrows
    private TimedReport setTimedReport(HashMap<String, List> interResult) {
        TimedReport timedReport = new TimedReport();

        for(int u = 0; u < interResult.get("request").size(); u++) {
            TimedReport.Batch batch = new TimedReport.Batch();
            batch.setRequest((int) interResult.get("request").get(u));
            batch.setResponse((int) interResult.get("response").get(u));
            batch.setError((int) interResult.get("error").get(u));
            batch.setSuccess((int) interResult.get("success").get(u));
            batch.setWfBroken((int) interResult.get("workflowBroken").get(u));
            batch.setSendError((int) interResult.get("sendError").get(u));
            batch.setStart(sdf2.parse((String) interResult.get("startTimes").get(u)));
            timedReport.getBatchList().add(batch);
        }

        timedReport.setPrefix(modules.getTablePrefix());
        timedReport.setModule(modules.getModuleName());
        timedReport.setType(modules.getType());
        timedReport.setStart(from);
        timedReport.setFinish(to);

        if(interResult.get("result").size() != 0) {
            timedReport.setRequest((int) interResult.get("result").get(0));
            timedReport.setResponse((int) interResult.get("result").get(1));
            timedReport.setSuccess((int) interResult.get("result").get(2));
            timedReport.setWfBroken((int) interResult.get("result").get(3));
            timedReport.setError((int) interResult.get("result").get(4));
        }
        executeWFB(timedReport);
        return timedReport;
    }

    private void executeWFB(TimedReport timedReport) {
        String sql = "";
        Gson gson = new Gson();
        sql = MessageFormat.format(
                SQLRequestsPostgre.WORKFLOWBROKENWITHTEXT.getRequest(),
                modules.getTablePrefix(),
                sdf.format(from),
                sdf.format(to),
                modules.getType().getReceiveRequests()
        );
        log.debug(sql);
        try(CallableStatement statement = connection.prepareCall(sql)) {
            try(ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    TimedReport.WorkflowBrokenInfo wfbi = new TimedReport.WorkflowBrokenInfo();
                    wfbi.setOperationId(rs.getString("OPERATION_ID"));
                    try {
                        String stest = rs.getString("ERROR");
                        if (!("}".equals(stest.substring(0, 1)))) {
                            stest = "{\"ERROR\":\"" + stest;
                        }
                        wfbi.setMessage(gson.fromJson(stest, HeaderType.class)
                                .getOrDefault("ERROR", "UNKNOWN")
                        );
                    } catch (JsonSyntaxException | NullPointerException e) {
                        log.warn("Error parsing error", e);
                        wfbi.setMessage(rs.getString("ERROR"));
                    } catch (RuntimeException e) {
                        log.warn("Some runtime exception", e);
                    }
                    wfbi.setCreationTime(rs.getString("START_TIME"));
                    wfbi.setBrokenTime(rs.getString("FINISH_TIME"));
                    timedReport.getWorkflowBrokenInfoList().add(wfbi);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public TimedReport executeSendReport(Date from, Date to, PostgreModules module) {
        log.info("Analyzing module {}", module.getModuleName());

        this.from = from;
        this.to = to;
        this.modules = module;

        return executeRequest(getSqlR());
    }
}
