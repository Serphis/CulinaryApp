package com.example.projektinz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetails extends AppCompatActivity {
    private String currentUserUsername;
    private List<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        TextView recipeNameTextView = findViewById(R.id.recipe_name);
        TextView recipeDescriptionTextView = findViewById(R.id.recipe_description);
        ListView ingredientListView = findViewById(R.id.ingredient_list);
        AppCompatButton returnButton = findViewById(R.id.return_button);
        ImageView recipeImageView = findViewById(R.id.recipe_image);
        AppCompatButton deleteButton = findViewById(R.id.delete_button);
        ListView instructionListView = findViewById(R.id.instruction_list);
        TextView recipeMadeByTextView = findViewById(R.id.recipe_made_by);
        Recipe selectedRecipe = getIntent().getParcelableExtra("selectedRecipe");
        if (selectedRecipe != null) {
            String imageURL = selectedRecipe.getImageURL();
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.baseline_error_outline_24)
                    .error(R.drawable.baseline_error_outline_24)
                    .into(recipeImageView);
            recipeNameTextView.setText(selectedRecipe.getName());
            recipeDescriptionTextView.setText(selectedRecipe.getDescription());
            List<Ingredient> ingredients = selectedRecipe.getIngredients();
            if (selectedRecipe.getMadeBy() != null) {
                recipeMadeByTextView.setText("Made by: " + selectedRecipe.getMadeBy());
            } else {
                recipeMadeByTextView.setText("Made by: Unknown");
            }
            List<String> ingredientNames = new ArrayList<>();
            for (Ingredient ingredient : ingredients) {
                ingredientNames.add(ingredient.getName());
            }
            ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, android.R.id.text1, ingredientNames);
            ingredientListView.setAdapter(ingredientAdapter);
        }
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            DatabaseReference usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
            Query query = usersRef.orderByChild("email").equalTo(currentUserEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            currentUserUsername = userSnapshot.child("username").getValue(String.class);
                        }
                        if (currentUserUsername != null && selectedRecipe != null
                                && selectedRecipe.getMadeBy() != null && selectedRecipe.getMadeBy().equals(currentUserUsername)) {
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setOnClickListener(v -> showDeleteConfirmation(selectedRecipe));
                        } else {
                            deleteButton.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(RecipeDetails.this, "Current user not found", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(RecipeDetails.this, "Error fetching current user: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        List<String> instructions = selectedRecipe.getInstructions();
        ArrayAdapter<String> instructionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, android.R.id.text1, instructions);
        instructionListView.setAdapter(instructionAdapter);
        returnButton.setOnClickListener(v -> openRecipeBrowser());
    }

    private void showDeleteConfirmation(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> deleteRecipe(recipe))
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void deleteRecipe(Recipe recipe) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("recipes");
        databaseReference.child(recipe.getName()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(RecipeDetails.this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                    DatabaseReference ingredientsRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("ingredients");
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        ingredientsRef.child(ingredient.getName()).removeValue();
                    }
                    openRecipeBrowser();
                } else {
                    Toast.makeText(RecipeDetails.this, "Error deleting recipe: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void openRecipeBrowser(){
        startActivity(new Intent(RecipeDetails.this, RecipeBrowser.class));
        finish();
    }
}
