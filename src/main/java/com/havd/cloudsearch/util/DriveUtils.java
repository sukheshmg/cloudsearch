package com.havd.cloudsearch.util;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

public class DriveUtils {
    static public Drive getDrive(String accessToken) {
        GoogleCredentials credentials = GoogleCredentials.newBuilder().
                setAccessToken(AccessToken.newBuilder().setTokenValue(accessToken).build()).build();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        com.google.api.services.drive.Drive drive = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
                requestInitializer).
                setApplicationName("Drive Search").
                build();

        return drive;
    }
}
