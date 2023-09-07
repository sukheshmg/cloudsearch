package com.havd.cloudsearch.service.impl.model;

import lombok.Data;

@Data
public class ESDocument {
    private String title;
    private String content;
    private String link;
}
