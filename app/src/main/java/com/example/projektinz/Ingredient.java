package com.example.projektinz;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Ingredient implements Parcelable {
    private String id, name;
    private float averageRating;
    private int totalRatings;
    private Map<String, Float> ratings;

    public Ingredient() {
        ratings = new HashMap<>();
    }

    public Ingredient(String name) {
        this.name = name;
        this.averageRating = 0.0f;
        this.totalRatings = 0;
        this.ratings = new HashMap<>();
    }

    protected Ingredient(Parcel in) {
        id = in.readString();
        name = in.readString();
        averageRating = in.readFloat();
        totalRatings = in.readInt();
        ratings = new HashMap<>();
        in.readMap(ratings, Float.class.getClassLoader());
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeFloat(averageRating);
        dest.writeInt(totalRatings);
        dest.writeMap(ratings);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public Map<String, Float> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Float> ratings) {
        this.ratings = ratings;
    }

    public void addRating(String userId, float rating) {
        if (ratings == null) {
            ratings = new HashMap<>();
        }

        ratings.put(userId, rating);
        recalculateAverageRating();
    }

    public float getRating(String userId) {
        Float userRating = ratings.get(userId);
        return userRating != null ? userRating : 0.0f;
    }

    protected void recalculateAverageRating() {
        float sum = 0.0f;
        for (float rating : ratings.values()) {
            sum += rating;
        }

        totalRatings = ratings.size();
        averageRating = totalRatings > 0 ? sum / totalRatings : 0.0f;
    }

    @Exclude
    @Override
    public String toString() {
        return name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> ingredientMap = new HashMap<>();
        ingredientMap.put("name", name);
        ingredientMap.put("averageRating", averageRating);
        ingredientMap.put("totalRatings", totalRatings);
        ingredientMap.put("ratings", ratings);
        return ingredientMap;
    }
}
