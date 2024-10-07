package com.example.projektinz;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private MaterialButton loginButton, registerButton;
    private ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        toggleButton = findViewById(R.id.show_password);
        loginButton.setOnClickListener(view -> {
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!password.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, MainPage.class));
                                finish();
                            }).addOnFailureListener(e ->
                                    Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                } else {
                    loginPassword.setError("Password cannot be empty");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
            } else {
                loginEmail.setError("Please enter a valid email");
            }
        });

        registerButton.setOnClickListener(v -> openRegisterPage());
    }

    public void openRegisterPage() {
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }

    public void togglePasswordVisibility(View view) {
        if (toggleButton.isChecked()) {
            loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }
}
