package com.example.doanthuctap.helper;

import android.app.Application;

import com.example.doanthuctap.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Phong-Kaster
 * Class nay duoc dung de luu bien toan cuc trong do an nay
 */
public class GlobalVariable extends Application {

    private String accessToken;
    private User AuthUser;
    private final String SHARED_PREFERENCE_KEY = "doanthuctap";
    private String contentType = "application/x-www-form-urlencoded";
    private Map<String, String> headers;

    /*** GETTER & SETTER ***/
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getAuthUser() {
        return AuthUser;
    }

    public void setAuthUser(User authUser) {
        AuthUser = authUser;
    }

    public String getSharedReferenceKey() {
        return SHARED_PREFERENCE_KEY;
    }

    public String getContentType() {
        return contentType;
    }

    /***
     * Create headers for API request
     */
    public Map<String, String> getHeaders() {
        headers = new HashMap<>();
        headers.put("Content-Type", contentType);

        if (accessToken != null) {
            headers.put("Authorization", "JWT " + accessToken);
        }

        return headers;
    }


}
