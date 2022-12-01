package com.utils.analyzer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Types {

    FIRSTSERVICEFORORACLE("REQUEST", "RESPONSE", "SUCCESS", ""),
    SECONDSERVICEFORORACLE("REQUEST", "RESPONSE", "SUCCESS", ""),
    THIRDSERVICEFORORACLE("REQUEST", "RESPONSE", "SUCCESS", ""),

    FIRSTSERVICEFORPOSTGRE("REQUEST", "RESPONSE", "SUCCESS", ""),
    SECONDSERVICEFORPOSTGRE("REQUEST", "RESPONSE", "SUCCESS", ""),
    THIRDSERVICEFORPOSTGRE("REQUEST", "RESPONSE", "SUCCESS", "");


    private String receiveRequests;
    private String sendResponse;
    private String success;
    private String optional;
}
