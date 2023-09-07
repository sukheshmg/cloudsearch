package com.havd.cloudsearch.ws.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    @NonNull
    private Result result;
    @NonNull
    private Error error;
}
