package com.havd.cloudsearch.ws.model.req;

import lombok.Data;
import lombok.NonNull;

import java.util.Set;

@Data
public class ChannelCreateRequest {
    @NonNull
    private String canonicalName;
    @NonNull
    private String accessToken;
    @NonNull
    private String refreshToken;
}
