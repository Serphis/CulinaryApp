package com.example.projektinz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyRecipes extends AppCompatActivity {
    private String currentUserEmail;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app").getReference("recipes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUserEmail = currentUser.getEmail();
        ListView myRecipesListView = findViewById(R.id.my_recipes_list);
        AppCompatButton returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(view -> finish());
        fetchUsernameAndLoadRecipes(myRecipesListView);
    }

    private void fetchUsernameAndLoadRecipes(ListView myRecipesListView) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        Query query = usersRef.orderByChild("email").equalTo(currentUserEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String uid = userSnapshot.getKey();
                        DatabaseReference userUidRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(uid);
                        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userUidSnapshot) {
                                if (userUidSnapshot.exists()) {
                                    String currentUsername = userUidSnapshot.child("username").getValue(String.class);
                                    if (currentUsername != null) {
                                        loadMyRecipesList(myRecipesListView, currentUsername);
                                    } else {
                                        Toast.makeText(MyRecipes.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MyRecipes.this, "User not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MyRecipes.this, "Failed to fetch username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                } else {
                    Toast.makeText(MyRecipes.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyRecipes.this, "Failed to fetch username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyRecipesList(ListView myRecipesListView, String currentUsername) {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<HashMap<String, Recipe>> genericTypeIndicator =
                        new GenericTypeIndicator<HashMap<String, Recipe>>() {};
                HashMap<String, Recipe> recipeMap = snapshot.getValue(genericTypeIndicator);
                if (recipeMap != null) {
                    List<Recipe> myRecipes = new ArrayList<>();
                    for (Recipe recipe : recipeMap.values()) {
                        if (recipe.getMadeBy() != null && recipe.getMadeBy().trim().equalsIgnoreCase(currentUsername.trim())) {
                            myRecipes.add(recipe);
                        }
                    }
                    ListView myRecipesListView = findViewById(R.id.my_recipes_list);
                    ArrayAdapter<Recipe> adapter = new ArrayAdapter<Recipe>(MyRecipes.this, R.layout.recipe_item,
                            R.id.recipeNameTextView, myRecipes) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(R.layout.recipe_item, parent, false);
                            }
                            TextView recipeNameTextView = convertView.findViewById(R.id.recipeNameTextView);
                            TextView madeByTextView = convertView.findViewById(R.id.madeByTextView);
                            Recipe recipe = (Recipe) getItem(position);
                            if (recipe != null) {
                                recipeNameTextView.setText("Name: " + recipe.getName());
                                madeByTextView.setText("Made By: " + recipe.getMadeBy());
                            }
                            return convertView;
                        }
                    };
                    myRecipesListView.setAdapter(adapter);
                    TextView noRecipesMessage = findViewById(R.id.no_recipes_message);
                    if (myRecipes.isEmpty()) {
                        noRecipesMessage.setVisibility(View.VISIBLE);
                    } else {
                        noRecipesMessage.setVisibility(View.GONE);
                    }
                    myRecipesListView.setOnItemClickListener((parent, view, position, id) -> {
                        Recipe selectedRecipe = myRecipes.get(position);
                        if (selectedRecipe != null) {
                            Intent intent = new Intent(MyRecipes.this, RecipeDetails.class);
                            intent.putExtra("selectedRecipe", selectedRecipe);
                            startActivity(intent);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyRecipes.this, "Error loading recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
