package com.example.projektinz;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Recipe implements Parcelable {
    private String id, name, description, imageURL, madeBy;
    private List<Ingredient> ingredients;
    private List<String> instructions;
    private float averageRating;
    private static final String defaultImage = "R.drawable.baseline_error_outline_24.xml";

    public Recipe() {
        this.imageURL = defaultImage;
    }

    public Recipe(String name, String description, List<Ingredient> ingredients, List<String> instructions, String imageURL, String madeBy) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageURL = defaultImage;
        this.madeBy = madeBy;
    }

    protected Recipe(Parcel in) {
        name = in.readString();
        description = in.readString();
        ingredients = new ArrayList<>();
        in.readList(ingredients, Ingredient.class.getClassLoader());
        instructions = new ArrayList<>();
        in.readList(instructions, String.class.getClassLoader());
        imageURL = in.readString();
        madeBy = in.readString();
        averageRating = in.readFloat();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeList(ingredients);
        dest.writeList(instructions);
        dest.writeString(imageURL);
        dest.writeString(madeBy);
        dest.writeFloat(averageRating);
        dest.writeFloat(getAverageIngredientRating());
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getImageURL() {
        return imageURL;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public String getMadeBy() {
        return madeBy;
    }
    public void setAverageIngredientRating() {
        if (ingredients != null && !ingredients.isEmpty()) {
            float sum = 0.0f;
            int count = 0;

            for (Ingredient ingredient : ingredients) {
                sum += ingredient.getAverageRating();
                count++;
            }

            averageRating = (count > 0) ? sum / count : 0.0f;
        }
    }

    public void setAverageRating() {
        if (ingredients != null && !ingredients.isEmpty()) {
            float sum = 0.0f;
            int count = 0;

            for (Ingredient ingredient : ingredients) {
                sum += ingredient.getAverageRating();
                count++;
            }

            averageRating = (count > 0) ? sum / count : 0.0f;
        }
    }
    public float getAverageIngredientRating() {
        float sum = 0.0f;
        int count = 0;

        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                sum += ingredient.getAverageRating();
                count++;
            }
        }

        return count > 0 ? sum / count : 0.0f;
    }
}
