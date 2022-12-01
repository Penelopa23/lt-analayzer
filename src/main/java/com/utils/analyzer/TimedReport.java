package com.utils.analyzer;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TimedReport extends Report {

    private List<Batch> batchList = new LinkedList<>();
    private List<WorkflowBrokenInfo> workflowBrokenInfoList = new LinkedList<>();
    private List<WorkflowBrokenInfo> sendErrorInfoList = new LinkedList<>();
    private List<WorkflowBrokenInfo> sendErrorDetailsInfoList = new LinkedList<>();
    private List<WorkflowBrokenInfo> sendErrorTypeInfoList = new LinkedList<>();


    public static class Batch extends Report {

    }

    @Data
    public static class WorkflowBrokenInfo {
        private String operationId;
        private String message;
        private String creationTime;
        private String brokenTime;
    }
}
