package com.example.doanthuctap.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HTTPService {

    public static final String APP_PATH =
            "http://192.168.0.106:8080/PTIT_Backend/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            instance = new Retrofit.Builder()
                    .baseUrl(APP_PATH)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return instance;
    }
}
