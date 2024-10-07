package com.example.projektinz;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculatorMealTest {

    @Test
    public void createCalculatorMeal_withValidData() {
        String mealName = "Meal";
        String calories = "300";

        CalculatorMeal calculatorMeal = new CalculatorMeal(mealName, calories);

        assertEquals(mealName, calculatorMeal.getName());
        assertEquals(calories, calculatorMeal.getCalories());
    }

    @Test
    public void createCalculatorMeal_withInvalidCalories() {
        String mealName = "Meal";
        String invalidCalories = "Text";

        CalculatorMeal calculatorMeal = new CalculatorMeal(mealName, invalidCalories);

        assertEquals(mealName, calculatorMeal.getName());
        assertEquals("0", calculatorMeal.getCalories());
    }

    @Test
    public void createCalculatorMeal_withEmptyMealName() {
        String emptyMealName = "";
        String calories = "300";

        CalculatorMeal calculatorMeal = new CalculatorMeal(emptyMealName, calories);

        assertEquals(emptyMealName, calculatorMeal.getName());
        assertEquals(calories, calculatorMeal.getCalories());
    }
}
