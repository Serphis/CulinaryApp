package com.example.projektinz;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        AppCompatButton browseRecipesButton = view.findViewById(R.id.browse_recipes_button);
        AppCompatButton tasteCreatorButton = view.findViewById(R.id.taste_creator_button);
        AppCompatButton addNewRecipeButton = view.findViewById(R.id.add_new_recipe_button);
        AppCompatButton calculatorButton = view.findViewById(R.id.calculator_button);

        browseRecipesButton.setOnClickListener(view1 -> openActivity(RecipeBrowser.class));
        addNewRecipeButton.setOnClickListener(view1 -> openActivity(AddNewRecipe.class));
        tasteCreatorButton.setOnClickListener(view1 -> openActivity(TasteCreator.class));
        calculatorButton.setOnClickListener(view1 -> openActivity(Calculator.class));
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
    }
}