package com.example.projektinz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainPage extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    MenuFragment menuFragment = new MenuFragment();
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, menuFragment).commit();
                return true;
            } else if (itemId == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                return true;
            } else if (itemId == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                return true;
            }
            return false;
        });
    }
}