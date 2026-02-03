package com.example.doanthuctap.activity.personality;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.doanthuctap.R;
import com.example.doanthuctap.helper.Dialog;
import com.example.doanthuctap.helper.GlobalVariable;
import com.example.doanthuctap.helper.LoadingScreen;
import com.example.doanthuctap.model.User;
import com.example.doanthuctap.viewModel.personality.ChangeInformationViewModel;

import java.util.Map;
import android.util.Log;
public class AccountInformationActivity extends AppCompatActivity {

    private EditText txtFirstName, txtLastName, txtPhone, txtAddress, txtEmail;
    private AppCompatButton buttonSave;
    private ImageButton buttonBack;

    private ChangeInformationViewModel viewModel;
    private GlobalVariable globalVariable;
    private Dialog dialog;
    private LoadingScreen loadingScreen;

    private User authUser;
    private Map<String, String> headers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_information);

        setupComponent();
        setupViewModel();
        setupScreen();
        setupEvent();
    }

    private void setupComponent() {
        txtFirstName = findViewById(R.id.changeInforFirstName);
        txtLastName  = findViewById(R.id.changeInforLastName);
        txtPhone     = findViewById(R.id.changeInforPhone);
        txtAddress   = findViewById(R.id.changeInforAddress);
        txtEmail     = findViewById(R.id.changeInforEmail);

        buttonSave = findViewById(R.id.changeInforButtonSave);
        buttonBack = findViewById(R.id.changeInforButtonGoBack);

        globalVariable = (GlobalVariable) getApplication();
        authUser = globalVariable.getAuthUser();

        dialog = new Dialog(this);
        loadingScreen = new LoadingScreen(this);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(ChangeInformationViewModel.class);

        viewModel.getAnimation().observe(this, isLoading -> {
            if (isLoading) loadingScreen.start();
            else loadingScreen.stop();
        });

        dialog.announce();

        viewModel.getResponse().observe(this, response -> {

            if (response == null) {
                dialog.show(
                        R.string.fail,
                        getString(R.string.oops_there_is_an_issue),
                        R.drawable.ic_close
                );
                dialog.btnOK.setOnClickListener(v -> dialog.close());
                return;
            }

            if (response.getResult() == 1 && response.getData() != null) {

                // ✅ UPDATE USER TOÀN CỤC
                globalVariable.setAuthUser(response.getData());

                dialog.show(
                        R.string.success,
                        getString(R.string.change_information_successfully),
                        R.drawable.ic_check
                );

                dialog.btnOK.setOnClickListener(v -> {
                    dialog.close();
                    finish(); // quay về Profile
                });

            } else {
                dialog.show(
                        R.string.fail,
                        response.getMsg(),
                        R.drawable.ic_close
                );
                dialog.btnOK.setOnClickListener(v -> dialog.close());
            }
        });
    }

    private void setupScreen() {
        authUser = globalVariable.getAuthUser(); // 🔥 LẤY LẠI Ở ĐÂY

        if (authUser == null) {
            dialog.show(
                    R.string.fail,
                    "Unauthorized",
                    R.drawable.ic_close
            );
            dialog.btnOK.setOnClickListener(v -> {
                dialog.close();
                finish();
            });
            return;
        }

        txtEmail.setText(authUser.getEmail());
        txtFirstName.setText(authUser.getFirstName());
        txtLastName.setText(authUser.getLastName());
        txtPhone.setText(authUser.getPhone());
        txtAddress.setText(authUser.getAddress());
    }

    private void setupEvent() {
        buttonSave.setOnClickListener(v -> {

            headers = globalVariable.getHeaders();
            Log.d("TOKEN", globalVariable.getAccessToken());
            Log.d("HEADERS", headers.toString());
            // ✅ CHECK TOKEN
            if (headers == null || !headers.containsKey("Authorization")) {
                dialog.show(
                        R.string.fail,
                        "Unauthorized",
                        R.drawable.ic_close
                );
                dialog.btnOK.setOnClickListener(x -> dialog.close());
                return;
            }

            String email     = txtEmail.getText().toString().trim();
            String firstName = txtFirstName.getText().toString().trim();
            String lastName  = txtLastName.getText().toString().trim();
            String phone     = txtPhone.getText().toString().trim();
            String address   = txtAddress.getText().toString().trim();

            // ✅ VALIDATE INPUT
            if (firstName.isEmpty() || lastName.isEmpty()) {
                dialog.show(
                        R.string.fail,
                        "First name & Last name are required",
                        R.drawable.ic_close
                );
                dialog.btnOK.setOnClickListener(x -> dialog.close());
                return;
            }

            viewModel.changeInformation(
                    headers,
                    email,
                    firstName,
                    lastName,
                    phone,
                    address
            );
        });

        buttonBack.setOnClickListener(v -> finish());
    }
}
