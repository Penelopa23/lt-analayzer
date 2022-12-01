package com.utils.analyzer.controllers;


import com.utils.analyzer.reportUtils.CreateReport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.utils.analyzer.oracle.CreateHtmlReportOracle.fileOracle;
import static com.utils.analyzer.oracle.CreateHtmlReportOracle.getTimedReportedOracle;
import static com.utils.analyzer.postgre.CreateHtmlReportPostgre.filePostgre;
import static com.utils.analyzer.postgre.CreateHtmlReportPostgre.getTimedReportedPostgre;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
@Controller
public class IndexController {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private final List<CreateReport> ll;
    private final StartParameters startParameters = new StartParameters();
    private Date from;
    private Date to;
//    public static List<TimedReport> getTimedReportedPostgre() { return reportlist; }
//    public static File filePostgre() { return filePostgre; }

    @GetMapping("/main")
    public String startParameters(Model model) {
        model.addAttribute("startParameters", startParameters);
        return "main";
    }

    @SneakyThrows
    @PostMapping("/analyzer")
    public String analyzer(StartParameters startParameters) {
        from = sdf.parse(startParameters.getFrom());
        to = sdf.parse(startParameters.getTo());
        log.info("Start analyzing");
        ll.parallelStream().forEach(report -> report.createReport(from, to, "/target"));
        log.info("Analyze completed");
        return "redirect:/results";
    }

    @GetMapping("/results")
    public String results() { return "/results"; }

    @SneakyThrows
    @RequestMapping(value = "/zip", produces = "results/zip")
    public void zipFiles(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"results-" + sdf2.format(from) + ".zip\"");
        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
        List<File> files = new ArrayList<>(4);
        files.add(fileOracle());
        files.add(filePostgre());

        for( File file : files) {
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
    }

    @GetMapping("/oracle")
    public String oracle(Model model) {
        long seconds = from.toInstant().until(to.toInstant(), ChronoUnit.SECONDS);
        model.addAttribute("timedReport", getTimedReportedOracle());
        model.addAttribute("second", seconds);
        model.addAttribute("startParameters", startParameters);
        return "oracle";
        }

    @GetMapping("/postgre")
    public String postgre(Model model) {
        long seconds = from.toInstant().until(to.toInstant(), ChronoUnit.SECONDS);
        model.addAttribute("timedReport", getTimedReportedPostgre());
        model.addAttribute("second", seconds);
        model.addAttribute("startParameters", startParameters);
        return "postgre";
    }

}
