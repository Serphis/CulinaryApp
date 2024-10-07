package com.example.projektinz;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CalculatorMeal {
    private String name, calories;
    public CalculatorMeal(){
    }

    public CalculatorMeal(String name, String calories) {
        this.name = (name != null) ? name : "";
        this.calories = (calories != null) ? calories : "";
    }

    public String getName() {
        return name;
    }
    public String getCalories() {
        return calories;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    @Override
    public String toString() {
        return name + " - " + calories + " kcal";
    }
}