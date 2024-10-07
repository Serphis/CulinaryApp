package com.example.projektinz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference usersRef, recipesRef;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
        recipesRef = FirebaseDatabase.getInstance("https://projektinz-bdb68-default-rtdb.europe-west1.firebasedatabase.app/").getReference("recipes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        TextView usernameTextView = view.findViewById(R.id.username);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String currentEmail = currentUser.getEmail();
            if (currentEmail != null) {
                fetchUsername(currentEmail, new UsernameFetchListener() {
                    @Override
                    public void onUsernameFetched(String username) {
                        if (username != null) {
                            usernameTextView.setText(username);
                        } else {
                            usernameTextView.setText("No username");
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        usernameTextView.setText("Error fetching username");
                    }
                });
            }
        }
        fetchRandomRecipes(new RandomRecipesFetchListener() {
            @Override
            public void onRecipesFetched(List<Recipe> randomRecipes) {
                displayRandomRecipes(randomRecipes);
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });
    }

    private void fetchUsername(String email, UsernameFetchListener listener) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String username = userSnapshot.child("username").getValue(String.class);
                        listener.onUsernameFetched(username);
                        return;
                    }
                }
                listener.onUsernameFetched(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    private void fetchRandomRecipes(RandomRecipesFetchListener listener) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<HashMap<String, Recipe>> genericTypeIndicator =
                        new GenericTypeIndicator<HashMap<String, Recipe>>() {};
                HashMap<String, Recipe> allRecipes = snapshot.getValue(genericTypeIndicator);

                if (allRecipes != null && !allRecipes.isEmpty()) {
                    List<Recipe> allRecipeList = new ArrayList<>(allRecipes.values());

                    Collections.sort(allRecipeList, new Comparator<Recipe>() {
                        @Override
                        public int compare(Recipe recipe1, Recipe recipe2) {
                            return Float.compare(recipe2.getAverageIngredientRating(), recipe1.getAverageIngredientRating());
                        }
                    });

                    List<Recipe> topRecipes = allRecipeList.subList(0, Math.min(2, allRecipeList.size()));
                    listener.onRecipesFetched(topRecipes);
                } else {
                    listener.onFailure("No recipes found in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Failed to fetch recipes: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to fetch recipes: ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRandomRecipes(List<Recipe> randomRecipes) {
        if (!isAdded()) {
            return;
        }

        ListView listView = view.findViewById(R.id.listview);
        RecipeAdapter adapter = new RecipeAdapter(requireContext(), R.layout.recipe_item, randomRecipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe selectedRecipe = randomRecipes.get(position);
            if (selectedRecipe != null) {
                Intent intent = new Intent(requireContext(), RecipeDetails.class);
                intent.putExtra("selectedRecipe", selectedRecipe);
                startActivity(intent);
            }
        });
    }

    private void fetchTopRatedRecipes(TopRatedRecipesFetchListener listener) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<HashMap<String, Recipe>> genericTypeIndicator =
                        new GenericTypeIndicator<HashMap<String, Recipe>>() {};
                HashMap<String, Recipe> allRecipes = snapshot.getValue(genericTypeIndicator);

                if (allRecipes != null && !allRecipes.isEmpty()) {
                    List<Recipe> allRecipeList = new ArrayList<>(allRecipes.values());
                    Collections.sort(allRecipeList, (recipe1, recipe2) -> Float.compare(recipe2.getAverageIngredientRating(), recipe1.getAverageIngredientRating()));
                    List<Recipe> topRatedRecipes = allRecipeList.subList(0, Math.min(2, allRecipeList.size()));
                    listener.onTopRatedRecipesFetched(topRatedRecipes);
                } else {
                    listener.onFailure("No recipes found in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Failed to fetch recipes: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to fetch recipes: ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTopRatedRecipes(List<Recipe> topRatedRecipes) {
        if (!isAdded()) {
            return;
        }

        ListView listView = view.findViewById(R.id.listview);
        RecipeAdapter adapter = new RecipeAdapter(requireContext(), R.layout.recipe_item, topRatedRecipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe selectedRecipe = topRatedRecipes.get(position);
            if (selectedRecipe != null) {
                Intent intent = new Intent(requireContext(), RecipeDetails.class);
                intent.putExtra("selectedRecipe", selectedRecipe);
                startActivity(intent);
            }
        });
    }

    private void fetchAndDisplayTopRatedRecipes() {
        fetchTopRatedRecipes(new TopRatedRecipesFetchListener() {
            @Override
            public void onTopRatedRecipesFetched(List<Recipe> topRatedRecipes) {
                displayTopRatedRecipes(topRatedRecipes);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle failure if needed
            }
        });
    }

    public interface TopRatedRecipesFetchListener {
        void onTopRatedRecipesFetched(List<Recipe> topRatedRecipes);
        void onFailure(String errorMessage);
    }

    interface RandomRecipesFetchListener {
        void onRecipesFetched(List<Recipe> randomRecipes);
        void onFailure(String errorMessage);
    }

    interface UsernameFetchListener {
        void onUsernameFetched(String username);
        void onFailure(String errorMessage);
    }

    private static class RecipeAdapter extends ArrayAdapter<Recipe> {
        private final Context context;
        private final int resource;
        private final List<Recipe> recipes;

        public RecipeAdapter(Context context, int resource, List<Recipe> recipes) {
            super(context, resource, recipes);
            this.context = context;
            this.resource = resource;
            this.recipes = recipes;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            }

            Recipe currentRecipe = recipes.get(position);
            ImageView imageView = convertView.findViewById(R.id.recipeImageView);
            TextView recipeNameTextView = convertView.findViewById(R.id.recipeNameTextView);
            TextView madeByTextView = convertView.findViewById(R.id.madeByTextView);

            imageView.setImageResource(R.drawable.baseline_error_outline_24);
            recipeNameTextView.setText(currentRecipe.getName());
            madeByTextView.setText("Made by: " + currentRecipe.getMadeBy());

            return convertView;
        }
    }
}
