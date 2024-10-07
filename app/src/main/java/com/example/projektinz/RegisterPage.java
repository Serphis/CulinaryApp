package com.example.projektinz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterPage extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        auth = FirebaseAuth.getInstance();
        TextView registerEmail = findViewById(R.id.register_email);
        TextView registerPassword = findViewById(R.id.register_password);
        TextView registerUsername = findViewById(R.id.register_username);
        MaterialButton confirmRegisterButton = findViewById(R.id.confirm_register_button);
        MaterialButton backButton = findViewById(R.id.back_button);
        confirmRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                String username = registerUsername.getText().toString().trim();
                if (email.isEmpty()) {
                    registerEmail.setError("Email cannot be empty");
                }
                if (password.isEmpty()) {
                    registerPassword.setError("Password cannot be empty");
                }
                if (username.isEmpty()) {
                    registerUsername.setError("Username cannot be empty");
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String currentUid = auth.getCurrentUser().getUid();
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
                                    usersRef.child(currentUid).child("email").setValue(email);
                                    usersRef.child(currentUid).child("username").setValue(username);
                                    Toast.makeText(RegisterPage.this, "Register Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterPage.this, MainActivity.class));
                                } else {
                                    Toast.makeText(RegisterPage.this, "Register Failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            }
        });

        backButton.setOnClickListener(v -> openMainPage());
    }
    public void openMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}