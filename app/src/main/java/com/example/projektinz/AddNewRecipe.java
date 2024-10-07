package com.example.projektinz;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddNewRecipe extends AppCompatActivity {
    private DatabaseReference database;
    protected List<Ingredient> recipeIngredients;
    protected List<String> recipeInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_recipe);
        database = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        recipeIngredients = new ArrayList<>();
        recipeInstructions = new ArrayList<>();
        MaterialButton returnButton = findViewById(R.id.return_button);
        MaterialButton createRecipeButton = findViewById(R.id.create_recipe_button);
        MaterialButton addIngredientButton = findViewById(R.id.add_ingredient_button);
        MaterialButton addInstructionButton = findViewById(R.id.add_instruction_button);

        createRecipeButton.setOnClickListener(v -> {
            if (validateRecipeFields()) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                getCurrentUsername(user, new UsernameFetchListener() {
                    @Override
                    public void onUsernameFetched(String username) {
                        if (username != null) {
                            writeNewRecipe(username);
                            Toast.makeText(AddNewRecipe.this, "Recipe created successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddNewRecipe.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(AddNewRecipe.this, "Failed to fetch username: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(AddNewRecipe.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });

        returnButton.setOnClickListener(v -> finish());
        addIngredientButton.setOnClickListener(v -> addIngredient());
        addInstructionButton.setOnClickListener(v -> addInstruction());
    }

    private void addIngredient() {
        EditText ingredientNameEditText = findViewById(R.id.ingredient_name);
        String ingredientName = ingredientNameEditText.getText().toString().trim();
        if (!ingredientName.isEmpty()) {
            Ingredient ingredient = new Ingredient(ingredientName);
            recipeIngredients.add(ingredient);
            displayIngredients();
            ingredientNameEditText.setText("");
        } else {
            Toast.makeText(AddNewRecipe.this, "Please enter an ingredient name", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayIngredients() {
        LinearLayout ingredientsLayout = findViewById(R.id.ingredients_layout);
        ingredientsLayout.removeAllViews();
        for (Ingredient ingredient : recipeIngredients) {
            LinearLayout ingredientItemLayout = new LinearLayout(this);
            ingredientItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            ingredientItemLayout.setBackgroundColor(getResources().getColor(R.color.lightgrey));
            TextView textView = new TextView(this);
            textView.setText(ingredient.getName());
            textView.setTextSize(20);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setPadding(50, 10, 50, 10);
            MaterialButton deleteButton = new MaterialButton(this, null,
                    com.google.android.material.R.style.Widget_MaterialComponents_Button_OutlinedButton);
            deleteButton.setText("X");
            deleteButton.setBackgroundColor(getResources().getColor(R.color.grey));
            deleteButton.setTextColor(getResources().getColor(R.color.black));
            deleteButton.setStrokeColorResource(R.color.grey);
            deleteButton.setOnClickListener(v -> deleteIngredient(ingredient));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    200, 100
            );
            params.setMargins(0, 10, 40, 0);
            params.gravity = Gravity.END;
            deleteButton.setLayoutParams(params);
            deleteButton.setGravity(Gravity.CENTER);
            ingredientItemLayout.addView(textView);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textView.setLayoutParams(textParams);
            ingredientItemLayout.addView(deleteButton);
            ingredientItemLayout.setGravity(Gravity.END);
            ingredientsLayout.addView(ingredientItemLayout);
        }
    }

    private void deleteIngredient(Ingredient ingredient) {
        recipeIngredients.remove(ingredient);
        displayIngredients();
    }

    private void addInstruction() {
        EditText instructionEditText = findViewById(R.id.recipe_instructions);
        String instruction = instructionEditText.getText().toString().trim();
        if (!instruction.isEmpty()) {
            recipeInstructions.add(instruction);
            displayInstructions();
            instructionEditText.setText("");
        } else {
            Toast.makeText(AddNewRecipe.this, "Please enter an instruction", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayInstructions() {
        LinearLayout instructionsLayout = findViewById(R.id.instructions_layout);
        instructionsLayout.removeAllViews();
        for (String instruction : recipeInstructions) {
            LinearLayout instructionItemLayout = new LinearLayout(this);
            instructionItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            instructionItemLayout.setBackgroundColor(getResources().getColor(R.color.lightgrey));
            TextView textView = new TextView(this);
            textView.setText(instruction);
            textView.setTextSize(20);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setPadding(50, 10, 50, 10);
            MaterialButton deleteButton = new MaterialButton(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Button_OutlinedButton);
            deleteButton.setText("X");
            deleteButton.setBackgroundColor(getResources().getColor(R.color.grey));
            deleteButton.setTextColor(getResources().getColor(R.color.black));
            deleteButton.setStrokeColorResource(R.color.grey);
            deleteButton.setOnClickListener(v -> deleteInstruction(instruction));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    200, 100
            );
            params.setMargins(0, 10, 40, 0);
            params.gravity = Gravity.END;
            deleteButton.setLayoutParams(params);
            deleteButton.setGravity(Gravity.CENTER);
            instructionItemLayout.addView(textView);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textView.setLayoutParams(textParams);
            instructionItemLayout.addView(deleteButton);
            instructionItemLayout.setGravity(Gravity.END);
            instructionsLayout.addView(instructionItemLayout);
        }
    }

    private void deleteInstruction(String instruction) {
        recipeInstructions.remove(instruction);
        displayInstructions();
    }

    public boolean validateRecipeFields() {
        EditText recipeNameEditText = findViewById(R.id.recipe_name);
        EditText recipeDescriptionEditText = findViewById(R.id.recipe_description);
        return !recipeNameEditText.getText().toString().isEmpty() &&
                !recipeDescriptionEditText.getText().toString().isEmpty();
    }

    private void writeNewRecipe(String username) {
        EditText recipeNameEditText =
                findViewById(R.id.recipe_name);
        EditText recipeDescriptionEditText = findViewById(R.id.recipe_description);
        String recipeName = recipeNameEditText.getText().toString();
        String recipeDescription = recipeDescriptionEditText.getText().toString();
        Recipe recipe = new Recipe(recipeName, recipeDescription, recipeIngredients,
                recipeInstructions, "@drawable/baseline_error_outline_24", username);
        database.child("recipes").child(recipe.getName()).setValue(recipe);
        for (Ingredient ingredient : recipeIngredients) {
            database.child("ingredients").child(ingredient.getName()).setValue(ingredient.toMap());
        }
    }

    private void getCurrentUsername(FirebaseUser user, UsernameFetchListener listener) {
        if (user != null) {
            String currentEmail = user.getEmail();
            fetchUsernameFromDatabase(currentEmail, listener);
        } else {
            listener.onFailure("User not authenticated");
        }
    }

    private void fetchUsernameFromDatabase(String email, UsernameFetchListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String fetchedUsername = snapshot.child("username").getValue(String.class);
                        if (fetchedUsername != null) {
                            listener.onUsernameFetched(fetchedUsername);
                        } else {
                            listener.onFailure("Username not found");
                        }
                        return;
                    }
                } else {
                    listener.onFailure("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure("Failed to fetch username: " + databaseError.getMessage());
            }
        });
    }

    public interface UsernameFetchListener {
        void onUsernameFetched(String username);
        void onFailure(String errorMessage);
    }
}
