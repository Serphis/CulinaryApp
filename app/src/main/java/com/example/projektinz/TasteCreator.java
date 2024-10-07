package com.example.projektinz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TasteCreator extends AppCompatActivity {
    public FirebaseAuth auth;
    protected DatabaseReference database;
    private String userId;
    private List<Ingredient> ingredientsToRate = new ArrayList<>();
    private ListView userRatingsListView;
    private ArrayAdapter<String> userRatingsAdapter;
    protected List<String> userRatingsList = new ArrayList<>();
    private int currentIngredientIndex = 0;
    protected DatabaseReference userRatingsRef;
    private String randomIngredientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taste_creator);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        userRatingsListView = findViewById(R.id.user_ratings_list);
        userRatingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userRatingsList);
        userRatingsListView.setAdapter(userRatingsAdapter);
        userRatingsRef = database.child("user_ratings").child(userId);

        AppCompatButton nextIngredientButton = findViewById(R.id.next_ingredient_button);
        AppCompatButton returnButton = findViewById(R.id.return_button);
        AppCompatButton rateAgainButton = findViewById(R.id.rate_again_button);
        rateAgainButton.setOnClickListener(v -> showRateAgainDialog());
        nextIngredientButton.setOnClickListener(v -> loadNextIngredient());
        returnButton.setOnClickListener(v -> finish());

        loadUserRatings();
        loadIngredientsToRate();
    }

    protected void saveUserRating(String ingredientName, float rating) {
        String userRatingPath = "user_ratings/" + userId + "/" + ingredientName;
        database.child(userRatingPath).setValue(rating);
    }

    protected void synchronizeRecipesWithDatabase(List<Recipe> recipesList) {
        DatabaseReference recipesRef = database.child("recipes");

        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> existingRecipes = (Map<String, Object>) dataSnapshot.getValue();

                if (existingRecipes == null) {
                    existingRecipes = new HashMap<>();
                }

                for (Recipe recipe : recipesList) {
                    recipe.setAverageRating();
                    existingRecipes.put(recipe.getName(), recipe);
                }

                recipesRef.setValue(existingRecipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    protected void loadUserRatings() {
        String userRatingsPath = "user_ratings/" + userId;
        userRatingsRef = database.child(userRatingsPath);

        userRatingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> newUserRatingsList = new ArrayList<>();

                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    String ingredientName = ingredientSnapshot.getKey();
                    float rating = ingredientSnapshot.getValue(Float.class);
                    newUserRatingsList.add(formatRatingInfo(ingredientName, rating));
                }

                userRatingsList.clear();
                userRatingsList.addAll(newUserRatingsList);
                userRatingsAdapter.notifyDataSetChanged();

                updateUIAfterRatingsLoaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    private boolean hasRatedAllIngredients() {
        for (Ingredient ingredient : ingredientsToRate) {
            if (!hasUserRatedIngredient(ingredient.getName())) {
                return false;
            }
        }
        return true;
    }

    private void handleDatabaseError(DatabaseError error) {
        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    protected boolean hasUserRatedIngredient(String ingredientName) {
        for (String ratingInfo : userRatingsList) {
            String[] parts = ratingInfo.split(": ");
            String ratedIngredientName = parts[0];
            if (ratedIngredientName.equals(ingredientName)) {
                return true;
            }
        }
        return false;
    }

    private void hideRatingUI() {
        TextView itemNameTextView = findViewById(R.id.item_name);
        itemNameTextView.setText("All ingredients have been rated");
        itemNameTextView.setVisibility(View.INVISIBLE);

        RatingBar userRatingBar = findViewById(R.id.user_rating_bar);
        userRatingBar.setVisibility(View.INVISIBLE);

        AppCompatButton nextIngredientButton = findViewById(R.id.next_ingredient_button);
        nextIngredientButton.setVisibility(View.INVISIBLE);
        nextIngredientButton.setOnClickListener(null);
    }

    private void updateUIAfterRatingsLoaded() {
        if (hasRatedAllIngredients()) {
            hideRatingUI();
        }
    }


    private void loadIngredientsToRate() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("recipes");

        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ingredientsToRate.clear();

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);

                    if (recipe != null && recipe.getIngredients() != null) {
                        ingredientsToRate.addAll(recipe.getIngredients());
                    }
                }

                handleLoadedIngredients();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    private void handleLoadedIngredients() {
        if (ingredientsToRate.isEmpty()) {
            TextView itemNameTextView = findViewById(R.id.item_name);
            itemNameTextView.setText("No ingredients to rate");
            disableRatingUI();
        } else {
            loadUserRatings();
        }
    }


    private String formatRatingInfo(String ingredientName, float rating) {
        return ingredientName + ": " + rating;
    }

    private String getRandomIngredientName() {
        Random random = new Random();
        int randomIndex = random.nextInt(ingredientsToRate.size());
        return ingredientsToRate.get(randomIndex).getName();
    }

    private void disableRatingUI() {
        TextView itemNameTextView = findViewById(R.id.item_name);
        itemNameTextView.setText("All ingredients have been rated");
        itemNameTextView.setVisibility(View.INVISIBLE);

        RatingBar userRatingBar = findViewById(R.id.user_rating_bar);
        userRatingBar.setVisibility(View.INVISIBLE);

        AppCompatButton nextIngredientButton = findViewById(R.id.next_ingredient_button);
        nextIngredientButton.setVisibility(View.INVISIBLE);
        nextIngredientButton.setOnClickListener(null);
    }

    private void loadNextIngredient() {
        if (ingredientsToRate.isEmpty() || currentIngredientIndex >= ingredientsToRate.size()) {
            TextView itemNameTextView = findViewById(R.id.item_name);
            if (userRatingsList.isEmpty()) {
                itemNameTextView.setText("All ingredients have been rated");
            } else {
                itemNameTextView.setText("No new ingredients to rate");
            }
            disableRatingUI();
            return;
        }

        Ingredient nextIngredient = ingredientsToRate.get(currentIngredientIndex);
        TextView itemNameTextView = findViewById(R.id.item_name);
        itemNameTextView.setText(nextIngredient.getName());
        RatingBar userRatingBar = findViewById(R.id.user_rating_bar);
        userRatingBar.setRating(0.0f);
        userRatingBar.setVisibility(View.VISIBLE);
        AppCompatButton nextIngredientButton = findViewById(R.id.next_ingredient_button);
        nextIngredientButton.setText("Next");
        nextIngredientButton.setOnClickListener(v -> onRateIngredient(nextIngredient));
    }

    private void onRateIngredient(Ingredient nextIngredient) {
        RatingBar userRatingBar = findViewById(R.id.user_rating_bar);
        float rating = userRatingBar.getRating();

        if (rating == 0.0f) {
            Toast.makeText(this, "Please rate the ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        nextIngredient.addRating(userId, rating);
        saveIngredientToDatabase(nextIngredient);
        saveUserRating(nextIngredient.getName(), rating);

        String ratingInfo = formatRatingInfo(nextIngredient.getName(), rating);
        updateUserRatingsList(ratingInfo);

        currentIngredientIndex++;
        randomIngredientName = getRandomIngredientName();
        loadNextIngredient();
    }

    private void saveIngredientToDatabase(Ingredient ingredient) {
        DatabaseReference ingredientRef = database.child("ingredients").child(ingredient.getName());
        ingredientRef.setValue(ingredient.toMap());
    }

    private void updateUserRatingsList(String ratingInfo) {
        if (!userRatingsList.contains(ratingInfo)) {
            userRatingsList.add(ratingInfo);
            userRatingsAdapter.notifyDataSetChanged();
        }
    }

    private void showRateAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate Again");
        List<String> ratedIngredients = new ArrayList<>(userRatingsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ratedIngredients);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedIngredientInfo = ratedIngredients.get(which);
            String[] parts = selectedIngredientInfo.split(": ");
            String selectedIngredientName = parts[0];
            showRatingDialogForIngredient(selectedIngredientName);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRatingDialogForIngredient(String ingredientName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate Again - " + ingredientName);
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_rate_again, null);
        builder.setView(dialogLayout);
        RatingBar ratingBar = dialogLayout.findViewById(R.id.dialog_rating_bar);
        builder.setPositiveButton("Next", (dialog, which) -> {
            float newRating = ratingBar.getRating();
            updateRatingForIngredient(ingredientName, newRating);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateRatingForIngredient(String ingredientName, float newRating) {
        DatabaseReference ingredientRef = database.child("ingredients").child(ingredientName);
        ingredientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ingredient existingIngredient = dataSnapshot.getValue(Ingredient.class);

                    if (existingIngredient != null) {
                        existingIngredient.addRating(userId, newRating);
                        existingIngredient.recalculateAverageRating();

                        DatabaseReference updatedIngredientRef = database.child("ingredients").child(ingredientName);
                        updatedIngredientRef.setValue(existingIngredient)
                                .addOnSuccessListener(aVoid -> {
                                    String ratingInfo = formatRatingInfo(existingIngredient.getName(), newRating);
                                    updateUserRatingsList(ratingInfo);

                                    String userRatingPath = "user_ratings/" + userId + "/" + ingredientName;
                                    database.child(userRatingPath).setValue(newRating)
                                            .addOnSuccessListener(aVoid1 -> {
                                                currentIngredientIndex++;
                                                randomIngredientName = getRandomIngredientName();
                                                loadNextIngredient();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(TasteCreator.this, "Error updating user_ratings data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TasteCreator.this, "Error updating data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TasteCreator.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Recipe> getRecipes() {
        final List<Recipe> recipesList = new ArrayList<>();

        DatabaseReference recipesRef = database.child("recipes");
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipesList.add(recipe);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        return recipesList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        loadIngredientsToRate();
        synchronizeRecipesWithDatabase(getRecipes());
    }
}
