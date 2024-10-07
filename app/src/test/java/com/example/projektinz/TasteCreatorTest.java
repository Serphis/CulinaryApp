package com.example.projektinz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
public class TasteCreatorTest {

    @Mock
    private DatabaseReference mockedDatabaseReference;

    @Mock
    private FirebaseAuth mockedFirebaseAuth;

    @Mock
    private FirebaseUser mockedFirebaseUser;

    @Mock
    private DatabaseReference mockedUserRatingsRef;

    private TasteCreator tasteCreator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tasteCreator = new TasteCreator();
        tasteCreator.database = mockedDatabaseReference;
        tasteCreator.userRatingsRef = mockedUserRatingsRef;

        when(mockedFirebaseAuth.getCurrentUser()).thenReturn(mockedFirebaseUser);
        tasteCreator.auth = mockedFirebaseAuth;
    }

    @Test
    public void hasUserRatedIngredient_IngredientIsRated() {
        String ingredientName = "Sugar";
        tasteCreator.userRatingsList = new ArrayList<>(Arrays.asList("Sugar: 4.0", "Salt: 3.5"));

        boolean result = tasteCreator.hasUserRatedIngredient(ingredientName);

        assertTrue(result);
    }

    @Test
    public void hasUserRatedIngredient_IngredientIsNotRated() {
        String ingredientName = "Pepper";
        tasteCreator.userRatingsList = new ArrayList<>(Arrays.asList("Sugar: 4.0", "Salt: 3.5"));

        boolean result = tasteCreator.hasUserRatedIngredient(ingredientName);

        assertFalse(result);
    }

    @Test
    public void hasUserRatedIngredient_withInvalidRating() {
        String ingredientName = "Salt";
        tasteCreator.userRatingsList = new ArrayList<>(Arrays.asList("Salt: 0", "Pepper: Rating"));

        boolean result = tasteCreator.hasUserRatedIngredient(ingredientName);

        assertFalse(result);
    }

}
