package com.example.projektinz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        AppCompatButton accountSettingsButton = view.findViewById(R.id.account_settings_button);

        accountSettingsButton.setOnClickListener(view1 -> openActivity(AccountSettings.class));

        updateUsernameTextView(view);
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
    }

    private void updateUsernameTextView(View view) {
        TextView usernameTextView = view.findViewById(R.id.username);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentUid = currentUser.getUid();
            usersRef.child(currentUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.getValue(String.class);
                    if (username != null && !username.isEmpty()) {
                        usernameTextView.setText(username);
                    } else {
                        usernameTextView.setText("No username");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    usernameTextView.setText("Error fetching username");
                }
            });
        } else {
            usernameTextView.setText("No username");
        }
    }
}
