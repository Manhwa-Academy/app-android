package com.example.doanthuctap.activity.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.example.doanthuctap.R;
import com.example.doanthuctap.activity.home.HomeActivity;
import com.example.doanthuctap.helper.Dialog;
import com.example.doanthuctap.helper.GlobalVariable;
import com.example.doanthuctap.helper.LoadingScreen;
import com.example.doanthuctap.model.User;
import com.example.doanthuctap.viewModel.authentication.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername, txtPassword;
    private TextView txtCreateAccount;
    private ImageButton btnGoogleLogin;
    private AppCompatButton btnLogin;

    private LoginViewModel viewModel;
    private LoadingScreen loadingScreen;
    private GlobalVariable globalVariable;
    private Dialog dialog;
    private SharedPreferences sharedPreferences;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LOGIN", "onCreate");

        setupGoogleLogin();
        setupComponent();
        setupViewModel();
        setupEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LOGIN", "onDestroy");
    }

    /* ================= GOOGLE LOGIN ================= */

    private void setupGoogleLogin() {
        GoogleSignInOptions googleSignInOption =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("662484602449-j28h119j2a7i3gvh11sei5mndocitmid.apps.googleusercontent.com")
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOption);
    }

    /* ================= UI ================= */

    private void setupComponent() {
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);

        loadingScreen = new LoadingScreen(this);
        globalVariable = (GlobalVariable) getApplication();
        dialog = new Dialog(this);

        sharedPreferences = getSharedPreferences(
                globalVariable.getSharedReferenceKey(),
                MODE_PRIVATE
        );
    }

    /* ================= VIEW MODEL ================= */

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Loading
        viewModel.getAnimation().observe(this, isLoading -> {
            if (isLoading) loadingScreen.start();
            else loadingScreen.stop();
        });

        // Dialog OK
        dialog.announce();
        dialog.btnOK.setOnClickListener(v -> dialog.close());

        // Login thường
        viewModel.getObjects().observe(this, loginResponse -> {
            if (loginResponse == null) {
                dialog.show(getString(R.string.attention),
                        getString(R.string.oops_there_is_an_issue),
                        R.drawable.ic_close);
                return;
            }

            if (loginResponse.getResult() == 1) {
                handleLoginSuccess(
                        loginResponse.getAccessToken(),
                        loginResponse.getData()
                );
            } else {
                dialog.show(getString(R.string.attention),
                        loginResponse.getMsg(),
                        R.drawable.ic_close);
            }
        });

        // Login Google (✔ observer đặt ĐÚNG CHỖ)
        viewModel.getAuthWithGoogleResponse().observe(this, response -> {
            if (response == null) return;

            if (response.getResult() == 1) {
                handleLoginSuccess(
                        response.getAccessToken(),
                        response.getData()
                );
            } else {
                dialog.show(getString(R.string.attention),
                        response.getMsg(),
                        R.drawable.ic_info);
            }
        });
    }

    /* ================= EVENTS ================= */

    private void setupEvent() {

        btnLogin.setOnClickListener(v -> {
            String username = txtUsername.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Vui lòng nhập đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(username, password);
        });

        txtCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startSignUpActivityForResult.launch(intent);
        });

        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startGoogleSignInForResult.launch(intent);
        });
    }

    /* ================= SIGN UP RESULT ================= */

    private final ActivityResultLauncher<Intent> startSignUpActivityForResult =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String email = result.getData().getStringExtra("email");
                            String password = result.getData().getStringExtra("password");
                            viewModel.login(email, password);
                        }
                    }
            );

    /* ================= GOOGLE RESULT ================= */

    private final ActivityResultLauncher<Intent> startGoogleSignInForResult =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            GoogleSignInAccount account =
                                    GoogleSignIn.getLastSignedInAccount(this);
                            if (account != null) {
                                createAccountWithGoogle(account);
                            }
                        }
                    }
            );

    /* ================= LOGIN HANDLER ================= */

    private void handleLoginSuccess(String token, User user) {
        // ✅ CHỈ LƯU TOKEN THUẦN (KHÔNG JWT)
        globalVariable.setAccessToken(token);
        globalVariable.setAuthUser(user);

        sharedPreferences.edit()
                .putString("accessToken", token) // ✅ KHÔNG JWT
                .apply();

        Toast.makeText(this,
                getString(R.string.login_successfully),
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }


    /* ================= GOOGLE LOGIN HANDLER ================= */

    private void createAccountWithGoogle(GoogleSignInAccount account) {
        String idToken = account.getIdToken();
        viewModel.authWithGoogleAccount(idToken);
    }
}
