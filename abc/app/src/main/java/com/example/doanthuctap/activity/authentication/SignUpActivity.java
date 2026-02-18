package com.example.doanthuctap.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.example.doanthuctap.R;
import com.example.doanthuctap.helper.Dialog;
import com.example.doanthuctap.helper.LoadingScreen;
import com.example.doanthuctap.viewModel.authentication.SignupViewModel;

public class SignUpActivity extends AppCompatActivity {

    private EditText txtFirstName, txtLastName, txtEmail, txtPassword, txtPasswordConfirm;
    private AppCompatButton buttonSignup;

    private SignupViewModel viewModel;
    private LoadingScreen loadingScreen;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setupComponent();
        setupViewModel();
        setupEvent();
    }

    private void setupComponent() {
        txtFirstName = findViewById(R.id.signUpFirstname);
        txtLastName = findViewById(R.id.signUpLastname);
        txtEmail = findViewById(R.id.signUpEmail);
        txtPassword = findViewById(R.id.signUpPassword);
        txtPasswordConfirm = findViewById(R.id.signUpPasswordConfirm);

        buttonSignup = findViewById(R.id.signUpButtonSignUp);

        loadingScreen = new LoadingScreen(this);
        dialog = new Dialog(this);
        dialog.announce();
        dialog.btnOK.setOnClickListener(v -> dialog.close());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        // Response
        viewModel.getResponse().observe(this, response -> {
            if (response == null) return;

            if (response.getResult() == 1) {
                Toast.makeText(this,
                        getString(R.string.sign_up_successfully),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.putExtra("email", txtEmail.getText().toString());
                intent.putExtra("password", txtPassword.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                dialog.show(
                        R.string.attention,
                        response.getMsg(),
                        R.drawable.ic_info
                );
            }
        });

        // Loading animation
        viewModel.getAnimation().observe(this, isLoading -> {
            if (isLoading) loadingScreen.start();
            else loadingScreen.stop();
        });
    }

    private void setupEvent() {
        buttonSignup.setOnClickListener(v -> {

            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            String passwordConfirm = txtPasswordConfirm.getText().toString().trim();
            String firstName = txtFirstName.getText().toString().trim();
            String lastName = txtLastName.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this,
                        "Please fill all required fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.signup(
                    email,
                    password,
                    passwordConfirm,
                    firstName,
                    lastName
            );
        });
    }
}
