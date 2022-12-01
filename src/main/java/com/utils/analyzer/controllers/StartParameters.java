package com.utils.analyzer.controllers;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StartParameters {
    private String from;
    private String to;
    private String path;
}
