package com.example.projektinz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Calculator extends AppCompatActivity {
    private EditText editTextCalorieGoal, editTextMealName, editTextCalories;
    private TextView textViewRemainingCalories;
    private List<CalculatorMeal> mealsList;
    private ArrayAdapter<CalculatorMeal> mealsAdapter;
    private static final String TAG = Calculator.class.getSimpleName();
    DatabaseReference database = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        AppCompatButton returnButton = findViewById(R.id.return_button);
        AppCompatButton btnClear = findViewById(R.id.btn_clear);
        editTextCalorieGoal = findViewById(R.id.editTextCalorieGoal);
        textViewRemainingCalories = findViewById(R.id.textViewRemainingCalories);
        ListView listViewMeals = findViewById(R.id.listViewMeals);
        mealsList = new ArrayList<>();
        mealsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mealsList);
        listViewMeals.setAdapter(mealsAdapter);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange called");
                mealsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.child("calculatorMeals").getChildren()) {
                    CalculatorMeal calculatorMeal = snapshot.getValue(CalculatorMeal.class);
                    Log.d(TAG, "Retrieved CalculatorMeal: " + calculatorMeal);
                    mealsList.add(calculatorMeal);
                }
                mealsAdapter.notifyDataSetChanged();
                updateRemainingCalories();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        btnClear.setOnClickListener(v -> clearMeals());
        returnButton.setOnClickListener(v -> finish());
    }

    public void openAddMealDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        editTextMealName = dialogView.findViewById(R.id.editTextMealName);
        editTextCalories = dialogView.findViewById(R.id.editTextCalories);
        AppCompatButton btnAddMeal = dialogView.findViewById(R.id.btnAddMeal);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        btnAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMeal(v);
                updateRemainingCalories();
                dialog.dismiss();
            }
        });
    }

    public void addMeal(View view) {
        String mealName = editTextMealName.getText().toString();
        String calories = editTextCalories.getText().toString();
        CalculatorMeal calculatorMeal = new CalculatorMeal(mealName, calories);
        database.child("calculatorMeals").push().setValue(calculatorMeal);
        Toast.makeText(Calculator.this,
                "Data inserted:" + mealName + " - " + calories + " kcal", Toast.LENGTH_SHORT).show();
        updateRemainingCalories();
    }

    private void clearMeals() {
        mealsList.clear();
        mealsAdapter.notifyDataSetChanged();
        database.child("calculatorMeals").removeValue();
        textViewRemainingCalories.setText("Calories left to eat: 0 kcal");
    }

    private void updateRemainingCalories() {
        try {
            int calorieGoal = Integer.parseInt(editTextCalorieGoal.getText().toString());
            int totalCalories = 0;
            for (CalculatorMeal meal : mealsList) {
                totalCalories += parseCalories(meal.getCalories());
            }
            int remainingCalories = calorieGoal - totalCalories;
            textViewRemainingCalories.setText("Calories left to eat: " + remainingCalories + " kcal");
        } catch (NumberFormatException e) {
            Toast.makeText(Calculator.this, "Wrong input.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private int parseCalories(String calories) {
        try {
            return Integer.parseInt(calories);
        } catch (NumberFormatException e) {
            Toast.makeText(Calculator.this, "Wrong input", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return 0;
        }
    }

}


