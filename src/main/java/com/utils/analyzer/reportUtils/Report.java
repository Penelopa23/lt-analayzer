package com.utils.analyzer.reportUtils;

import com.utils.analyzer.utils.Types;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Report {

    private Date start;
    private Date finish;
    private String name;
    private String module;
    private Types type;
    private String prefix;

    private int request;
    private int success;

    private float avgTimeOperations;
    private float avgTimePack;
    private float maxTimeOperation;
    private float minTimeOperation;

    private int response;
    private int wfBroken;
    private int okPercentil;
    private int rsPercentil;
    private int tps;
    private int duration;
    private int error;
    private int sendError;
    private int SendErrorDetail;
}
