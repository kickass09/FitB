package com.example.fitb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class CalorieCount extends AppCompatActivity {


    //private EditText editTextExercise;
    private EditText editTextSets;
    private EditText editTextReps;
    private EditText editTextWeight;
    private EditText editTextExerciseDuration;
    private TextView textViewVolume;
    private TextView textViewIntensity;
    private TextView textViewCaloriesBurned;
    private Spinner spinnerExercise;

    private int totalVolume = 0;
    private double caloriesPerMinute = 10.0; // Example value, replace with appropriate value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_count);

        spinnerExercise = findViewById(R.id.spinnerExercise);

        //editTextExercise = findViewById(R.id.editTextExercise);
        editTextSets = findViewById(R.id.editTextSets);
        editTextReps = findViewById(R.id.editTextReps);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextExerciseDuration = findViewById(R.id.editTextExerciseDuration);
        textViewVolume = findViewById(R.id.textViewVolume);
        textViewIntensity = findViewById(R.id.textViewIntensity);
        textViewCaloriesBurned = findViewById(R.id.textViewCaloriesBurned);



        Button buttonAddExercise = findViewById(R.id.buttonAddExercise);
        buttonAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExercise();
            }
        });
    }

    private void addExercise() {

        String exercise = spinnerExercise.getSelectedItem().toString();
        //String exercise = editTextExercise.getText().toString();
        int sets = Integer.parseInt(editTextSets.getText().toString());
        int reps = Integer.parseInt(editTextReps.getText().toString());
        double weight = Double.parseDouble(editTextWeight.getText().toString());
        int duration = Integer.parseInt(editTextExerciseDuration.getText().toString());

        int volume = sets * reps;
        double intensity = (weight * volume) / 1000.0; // Example calculation for intensity

        totalVolume += volume;
        textViewVolume.setText("Total Volume: " + totalVolume +"sets * reps");
        textViewIntensity.setText("Intensity: " + intensity +"kg");


        double caloriesPerMinute = calculateCaloriesPerMinute(exercise, weight);
        double caloriesBurned = caloriesPerMinute * duration;
        textViewCaloriesBurned.setText("Calories Burned: " + caloriesBurned +"kcal");

        // Additional calculations or tracking logic can be added here

        //editTextExercise.setText("");
        editTextSets.setText("");
        editTextReps.setText("");
        editTextWeight.setText("");
        editTextExerciseDuration.setText("");
    }

    private double calculateCaloriesPerMinute(String exercise, double weight) {
        // Use exercise-specific coefficients or MET values for calorie calculations
        if (exercise.equalsIgnoreCase("running")) {
            double metValue = 8.0; // Example MET value for running
            return metValue * weight;
        } else if (exercise.equalsIgnoreCase("Cycling (stationary bike)")) {
            double metValue = 6.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Weightlifting (general)")) {
            double metValue = 3.5; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("High-intensity Interval Training (HIIT)")) {
            double metValue = 10.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Elliptical training")) {
            double metValue = 5.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Rowing")) {
            double metValue = 7.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Circuit training")) {
            double metValue = 6.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Swimming")) {
            double metValue = 6.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Group fitness classes (e.g., Zumba, Aerobics)")) {
            double metValue = 6.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Stair climbing")) {
            double metValue = 9.0; // Example MET value for cycling
            return metValue * weight;
        }
        else if (exercise.equalsIgnoreCase("Treadmill running")) {
            double metValue = 8.0; // Example MET value for cycling
            return metValue * weight;
        }
        else {
            // Add additional exercise cases or implement a lookup table
            return 0.0; // Default value if exercise not found
        }
    }
}