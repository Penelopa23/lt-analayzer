package com.utils.analyzer.postgre;

import com.utils.analyzer.reportUtils.CreateReport;
import com.utils.analyzer.reportUtils.TimedReport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateHtmlReportPostgre implements CreateReport {
    private static List<TimedReport> reportList = new LinkedList<>();
    private static File filePostgre;

    private static final String HTML_TO_OPEN = "<td>";
    private static final String HTMLTO_CLOSE = "</td>";
    private static final String HTML_TO_WARN = "<td bgcolor=\"dfe929\">";
    private static final String HTML_TO_ERROR = "<td bgcolor=\"B22222\">";
    private static final String PERCENT = "%";

    private final AnalyzePostgre analyzePostgre;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");


    public static File filePostgre() { return filePostgre; }

    public static List<TimedReport> getTimedReportedPostgre() { return reportList; }

    @Override
    public void createReport(Date from, Date to, String path) {
        reportList = Arrays.stream(PostgreModules.values())
                .map(module -> analyzePostgre.executeSendReport(from, to, module))
                .collect(Collectors.toList());
        createHtmlReport(from, to, path);
    }

    private void createHtmlReport(Date from, Date to, String path) {
        long seconds = from.toInstant().until(to.toInstant(), ChronoUnit.SECONDS);
        StringBuilder html = new StringBuilder("<html><head><META charset=\"chcp 1251\"><title>Report")
                .append(sdf2.format(from))
                .append("</title></head><body>");
        html.append("<h1 align=\"center\">Результат НТ</h1>");
        html.append("<br align=\"left\"/><b>Начало:     </b>").append(sdf.format(from));
        html.append("<br align=\"left\"/><b>Окончание : </b>").append(sdf.format(to));
        html.append("<table border=3 cellpadding=10 cellspacing=1><tr>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Сервис</font></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Префикс</font><sup>1</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">TPS (факт)</font><sup>2</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Запросов всего</font><sup>3</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Успешно</font><sup>4</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Ответов</font><sup>5</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Неудачно</font><sup>6</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент неудач</font></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Без ответов</font><sup>7</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент без ответов</font></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Ошибки ответов</font><sup>8</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент ошибок без ответов</font></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Ошибки</font><sup>9</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент ошибок</font></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Остановка операции</font><sup>10</sup></th>");
        html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент остановок операций</font></th>");

        int p;
        float f;
        for(TimedReport t : reportList) {
            html.append("<tr>");
            html.append(HTML_TO_OPEN).append(t.getModule()).append(HTMLTO_CLOSE);
            html.append(HTML_TO_OPEN).append(t.getPrefix()).append(HTMLTO_CLOSE);
            html.append(HTML_TO_OPEN)
                    .append(String.format("%.2f", 1.0f * t.getRequest() / seconds))
                    .append(HTMLTO_CLOSE);
            html.append(HTML_TO_OPEN).append(t.getRequest()).append(HTMLTO_CLOSE);
            html.append(HTML_TO_OPEN).append(t.getSuccess()).append(HTMLTO_CLOSE);
            html.append(HTML_TO_OPEN).append(t.getResponse()).append(HTMLTO_CLOSE);
            p = t.getRequest() - t.getSuccess();
            f = 100.0f * p / t.getRequest();
            html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
            html.append(percentRow(f));
            p = t.getRequest() - t.getResponse();
            f = 100.0f * p / t.getRequest();
            html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
            html.append(percentRow(f));
            p = t.getWfBroken();
            f = 100.0f * p / t.getRequest();
            html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
            html.append(percentRow(f));
            html.append("</tr>");
        }
        html.append("</table>");

        html.append("<br/><h2>Статистика по сервисам</h2><br/>");
        for(TimedReport t : reportList) {
            if(t.getRequest() == 0) continue;
            html.append("<h3>").append(t.getModule()).append("/<h3>");
            html.append("<h4>Цепочки</h4>");
            html.append("<table border=3 cellpadding=10 cellspacing=1><tr>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Пачка</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Запросов</font><sup>3</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Успешно</font><sup>4</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Ответов</font><sup>5</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Неудачно</font><sup>6</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент неудач</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Без ответов</font><sup>7</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент без ответов</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Ошибок</font><sup>8</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент ошибок</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Остановка операции</font><sup>9</sup></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Процент остановок операций</font></th>");
            html.append("</tr>");

            for(TimedReport.Batch b : t.getBatchList()) {
                if(b.getRequest() == 0) continue;
                html.append("<tr>");
                html.append(HTML_TO_OPEN).append(sdf.format(b.getStart())).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(b.getRequest()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(b.getSuccess()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(b.getResponse()).append(HTMLTO_CLOSE);
                p = b.getRequest() - b.getSuccess();
                f = 100.0f * p / b.getRequest();
                html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
                html.append(percentRow(f));
                p = b.getRequest() - b.getResponse();
                f = 100.0f * p / b.getRequest();
                html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
                html.append(percentRow(f));
                p = b.getError();
                f = 100.0f * p / b.getRequest();
                html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
                html.append(percentRow(f));
                p = b.getWfBroken();
                f = 100.0f * p / b.getRequest();
                html.append(HTML_TO_OPEN).append(p).append(HTMLTO_CLOSE);
                html.append(percentRow(f));
                html.append("</tr>");
            }
            html.append("</table>");

            html.append("<h4>Ошибки</h4>");
            html.append("<table border=3 cellpadding=10 cellspacing=1><tr>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Количество</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Время запросов</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Время ошибки</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Текст ошибки</font></th>");

            for(TimedReport.WorkflowBrokenInfo w : t.getWorkflowBrokenInfoList()) {
                if(Objects.isNull(w.getMessage()) || StringUtils.isEmpty(w.getMessage())) continue;
                html.append("<tr>");
                html.append(HTML_TO_OPEN).append(w.getOperationId()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getCreationTime()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getBrokenTime()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getMessage()).append(HTMLTO_CLOSE);
                html.append("</tr>");
            }
            html.append("</table>");

            html.append("<h4>Ошибки отправки потребителю</h4>");
            html.append("<table border=3 cellpadding=10 cellspacing=1><tr>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Количество</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Время запросов</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Время ошибки</font></th>");
            html.append("<th bgcolor=\"#2F4F4F\"><font color=\"white\">Текст ошибки</font></th>");

            for(TimedReport.WorkflowBrokenInfo w : t.getSendErrorDetailsInfoList()) {
                if(Objects.isNull(w.getMessage()) || StringUtils.isEmpty(w.getMessage())) continue;
                html.append("<tr>");
                html.append(HTML_TO_OPEN).append(w.getOperationId()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getCreationTime()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getBrokenTime()).append(HTMLTO_CLOSE);
                html.append(HTML_TO_OPEN).append(w.getMessage()).append(HTMLTO_CLOSE);
                html.append("</tr>");
            }
            html.append("</table>");
        }
        html.append("<h4/><br/><h6>Generated at ").append(sdf.format(new Date())).append("</h6>");
        html.append("<br/").append(info());
        saveReport(from, to, path, html);

    }

    @SneakyThrows
    private void saveReport(Date from, Date to, String path, StringBuilder html) {
        String dirName = path + "\\" + sdf2.format(from).replace("-", "") +
                "-" + sdf2.format(to).replace("-", "");
        File sDirName = new File(dirName);
        File dirReport =sDirName;
        dirReport.mkdir();
        log.info("Отчёт сохранён в директорию {}", dirReport.getPath());
        String stand = "POSTGRE";
        String fileName = dirName + "/"/*path*/ + "/" + "Aggregation report -" + stand + "-" +
                sdf2.format(from) + "-" + System.currentTimeMillis() + ".html";
        File file = new File(fileName);
        filePostgre = file;
        try(FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(html.toString());
        }
    }

    public static StringBuilder percentRow(float f) {
        StringBuilder sb = new StringBuilder();
        if(f == 0) {
            sb.append(HTML_TO_OPEN);
        } else if (f >0 && f < 1) {
            sb.append(HTML_TO_WARN);
        } else {
            sb.append(HTML_TO_ERROR);
        }
        sb.append(String.format("%.2f", f)).append(PERCENT).append(HTMLTO_CLOSE);
        return sb;
    }

    private static StringBuilder info() {
        StringBuilder sb = new StringBuilder();
        sb.append("1. <b>Префикс</b> -- префикс таблицы в базе данных");
        sb.append("<br/>2. <b>TPS</b> -- Фактическое количество запросов в секунду во время теста");
        sb.append("<br/>3. <b>Запросов всего</b> -- количество сделанных запросов за время теста");
        sb.append("<br/>4. <b>Успешно</b> -- количество операций, в которых присутствует статусное сообщение об успешности");
        sb.append("<br/>5. <b>Ответов</b> -- Количество операций по которым был отправлен ответ потребителю");
        sb.append("<br/>6. <b>Неудачно</b> -- Количество операций по которым не был получен успешный ответ");
        sb.append("<br/>7. <b>Без ответов</b> -- Количество операций по которым не был отправлен ответ потребителю");
        sb.append("<br/>8. <b>Ошибок</b> -- Количество операций, которые содержали ошибку");
        sb.append("<br/>9. <b>Workflow Broken</b> -- Количестов операций которые были прерваны");
        sb.append("<br/>10. <b>Workflow Broken</b> -- префикс таблицы в базе данных");
        return sb;
    }

}
