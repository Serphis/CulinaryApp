package com.example.projektinz;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecipeBrowser extends AppCompatActivity {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("recipes");
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_browser);
        ListView recipeListView = findViewById(R.id.recipe_list);
        AppCompatButton returnButton = findViewById(R.id.return_button);
        AppCompatButton myRecipesButton = findViewById(R.id.my_recipes_button);
        currentUserEmail = getIntent().getStringExtra("userEmail");
        FrameLayout container = findViewById(R.id.container);
        container.setVisibility(View.GONE);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<HashMap<String, Recipe>> genericTypeIndicator =
                        new GenericTypeIndicator<HashMap<String, Recipe>>() {};
                HashMap<String, Recipe> recipeMap = snapshot.getValue(genericTypeIndicator);
                if (recipeMap != null) {
                    List<Recipe> recipeList = new ArrayList<>(recipeMap.values());
                    ArrayList<String> recipeInfoList = new ArrayList<>();
                    for (Recipe recipe : recipeList) {
                        String recipeInfo = "Name: " + recipe.getName() + "\nMade By: " + recipe.getMadeBy();
                        recipeInfoList.add(recipeInfo);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(RecipeBrowser.this,
                            R.layout.recipe_item, R.id.recipeNameTextView, recipeInfoList);
                    recipeListView.setAdapter(adapter);
                    recipeListView.setOnItemClickListener((parent, view, position, id) -> {
                        Recipe selectedRecipe = recipeList.get(position);
                        if (selectedRecipe != null) {
                            Intent intent = new Intent(RecipeBrowser.this, RecipeDetails.class);
                            intent.putExtra("selectedRecipe", selectedRecipe);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        returnButton.setOnClickListener(view -> finish());

        myRecipesButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeBrowser.this, MyRecipes.class);
            intent.putExtra("userEmail", currentUserEmail);
            startActivity(intent);
        });
    }
}
