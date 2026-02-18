package com.example.doanthuctap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.doanthuctap.activity.authentication.LoginActivity;
import com.example.doanthuctap.activity.home.HomeActivity;
import com.example.doanthuctap.helper.Dialog;
import com.example.doanthuctap.helper.GlobalVariable;
import com.example.doanthuctap.model.User;
import com.example.doanthuctap.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private GlobalVariable globalVariable;
    private SharedPreferences sharedPreferences;
    private MainViewModel viewModel;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupVariable();

        String accessToken = sharedPreferences.getString("accessToken", null);

        // 🔐 Có token → gọi API profile
        if (accessToken != null) {
            globalVariable.setAccessToken(accessToken);
            viewModel.getProfile(accessToken);
        } else {
            // ❌ Không có token → đi Login luôn
            goToLoginWithDelay();
        }

        // 👀 Observe profile response
        viewModel.profileObject().observe(this, profileResponse -> {

            // ❌ API fail / null
            if (profileResponse == null) {
                goToLoginWithDelay();
                return;
            }

            // ✅ Token hợp lệ
            if (profileResponse.getResult() == 1) {
                User user = profileResponse.getData();
                globalVariable.setAuthUser(user);
                goToHomeWithDelay();
            }
            // ❌ Token sai / hết hạn
            else {
                goToLoginWithDelay();
            }
        });
    }

    // =========================
    // ⏩ NAVIGATION
    // =========================

    private void goToHomeWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }, 1000);
    }

    private void goToLoginWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }, 1000);
    }

    // =========================
    // ⚙️ INIT
    // =========================

    private void setupVariable() {
        globalVariable = (GlobalVariable) getApplication();

        sharedPreferences = getSharedPreferences(
                globalVariable.getSharedReferenceKey(),
                MODE_PRIVATE
        );

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        dialog = new Dialog(this);
    }
}
