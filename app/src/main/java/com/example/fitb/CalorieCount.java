package com.example.fitb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private TextView textViewSets;
    private TextView textViewReps;
    private TextView textViewWeight;
    private TextView textViewIntensity;
    private TextView textViewCaloriesBurned;
    private Spinner spinnerExercise;

    private int totalVolume = 0;
    private double caloriesPerMinute = 10.0; // Example value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_count);

        spinnerExercise = findViewById(R.id.spinnerExercise);

        textViewSets=findViewById(R.id.textViewSet);
        textViewReps=findViewById(R.id.textViewReps);
        textViewWeight=findViewById(R.id.textViewWeight);
        editTextSets = findViewById(R.id.editTextSets);
        editTextReps = findViewById(R.id.editTextReps);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextExerciseDuration = findViewById(R.id.editTextExerciseDuration);
        textViewVolume = findViewById(R.id.textViewVolume);
        textViewIntensity = findViewById(R.id.textViewIntensity);
        textViewCaloriesBurned = findViewById(R.id.textViewCaloriesBurned);

        ArrayAdapter<CharSequence> exerciseAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.exercise_options,
                android.R.layout.simple_spinner_item
        );
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercise.setAdapter(exerciseAdapter);

        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedExercise = adapterView.getItemAtPosition(position).toString();
                updateSetsRepsVisibility(selectedExercise);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        Button buttonAddExercise = findViewById(R.id.buttonAddExercise);
        buttonAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExercise();
            }
        });
    }


    private void updateSetsRepsVisibility(String exercise) {
        if (exercise.equalsIgnoreCase("Weightlifting (general)")
                || exercise.equalsIgnoreCase("High-intensity Interval Training (HIIT)")
                || exercise.equalsIgnoreCase("Rowing")
                || exercise.equalsIgnoreCase("Circuit training")) {
            editTextSets.setVisibility(View.VISIBLE);
            editTextReps.setVisibility(View.VISIBLE);
            textViewSets.setVisibility(View.VISIBLE);
            textViewReps.setVisibility(View.VISIBLE);
            textViewWeight.setText("Weight in Kg (Lifting object):");

        } else {
            textViewWeight.setText("Weight in Kg (Body weight):");
            textViewSets.setVisibility(View.GONE);
            textViewReps.setVisibility(View.GONE);
            editTextSets.setVisibility(View.GONE);
            editTextReps.setVisibility(View.GONE);
        }
    }

    private void addExercise() {

        String exercise = spinnerExercise.getSelectedItem().toString();
        int sets = 0;
        int reps = 0;
        double weight = 0.0;
        int duration = 0;

        if (editTextSets.getVisibility() == View.VISIBLE) {
            String setsStr = editTextSets.getText().toString();
            if (!setsStr.isEmpty()) {
                sets = Integer.parseInt(setsStr);
            } else {
                editTextSets.setError("Enter sets");
                return; // Exit the method if sets are not filled
            }
        }
        if (editTextReps.getVisibility() == View.VISIBLE) {
            String repsStr = editTextReps.getText().toString();
            if (!repsStr.isEmpty()) {
                reps = Integer.parseInt(repsStr);
            } else {
                editTextReps.setError("Enter reps");
                return; // Exit the method if reps are not filled
            }
        }
        String weightStr = editTextWeight.getText().toString();
        if (!weightStr.isEmpty()) {
            weight = Double.parseDouble(weightStr);
        } else {
            editTextWeight.setError("Enter weight");
            return; // Exit the method if weight is not filled
        }

        String durationStr = editTextExerciseDuration.getText().toString();
        if (!durationStr.isEmpty()) {
            duration = Integer.parseInt(durationStr);
        } else {
            editTextExerciseDuration.setError("Enter duration");
            return; // Exit the method if duration is not filled
        }

        int volume = sets * reps;
        double intensity = (weight * volume) / 1000.0; // Example calculation for intensity

        totalVolume += volume;
        textViewVolume.setText("Total Volume: " + totalVolume +" sets * reps");
        textViewIntensity.setText("Intensity: " + intensity +" kg");


        double caloriesPerMinute = calculateCaloriesPerMinute(exercise, weight);
        double caloriesBurned = caloriesPerMinute * duration;
        textViewCaloriesBurned.setText("Calories Burned: " + caloriesBurned +" kcal");


        editTextSets.setText("");
        editTextReps.setText("");
        editTextWeight.setText("");
        editTextExerciseDuration.setText("");
    }

    private double calculateCaloriesPerMinute(String exercise, double weight) {
        // Used exercise-specific coefficients or MET values for calorie calculations
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
            return 0.0;
        }
    }
}