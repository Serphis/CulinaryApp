package com.example.projektinz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountSettings extends AppCompatActivity {

    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private TextView currentUsernameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        MaterialButton changeUsernameButton = findViewById(R.id.change_username_button);
        MaterialButton returnButton = findViewById(R.id.return_button);

        currentUsernameTextView = findViewById(R.id.current_username);

        changeUsernameButton.setOnClickListener(v -> showChangeUsernameDialog());
        returnButton.setOnClickListener(v -> returnToProfileFragment());
    }

    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_username, null);
        builder.setView(dialogView);
        EditText newUsernameEditText = dialogView.findViewById(R.id.new_username);
        TextView currentUsernameTextView = dialogView.findViewById(R.id.current_username);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button closeButton = dialogView.findViewById(R.id.close_button);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentUid = currentUser.getUid();
            usersRef.child(currentUid).child("username").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String currentUsername = task.getResult().getValue(String.class);
                    if (currentUsername != null && !currentUsername.isEmpty()) {
                        currentUsernameTextView.setText("Current Username: " + currentUsername);
                    } else {
                        currentUsernameTextView.setText("No username");
                    }
                } else {
                    showToast("Error fetching current username. Please try again.");
                }
            });
        } else {
            showToast("User not logged in.");
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        saveButton.setOnClickListener(v -> {
            String newUsername = newUsernameEditText.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                setUsernameForCurrentUser(newUsername);
                currentUsernameTextView.setText("Current Username: " + newUsername);
                showToast("Username set successfully: " + newUsername);
                alertDialog.dismiss();
            } else {
                showToast("Please enter a username.");
            }
        });

        closeButton.setOnClickListener(v -> alertDialog.dismiss());
    }



    private void setUsernameForCurrentUser(String newUsername) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentEmail = currentUser.getEmail();
            usersRef.child(auth.getCurrentUser().getUid()).child("username").setValue(newUsername);
            usersRef.child(auth.getCurrentUser().getUid()).child("email").setValue(currentEmail);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void returnToProfileFragment() {
        finish();
    }
}
