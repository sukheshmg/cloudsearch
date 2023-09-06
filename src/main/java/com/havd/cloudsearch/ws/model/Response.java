package com.havd.cloudsearch.ws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    private Result result;
    private Error error;
}
