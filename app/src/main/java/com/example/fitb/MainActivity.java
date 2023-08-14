package com.example.fitb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;

    private Spinner spinnerFilter;
    private String selectedFilter;

    // ExerciseDB API constants
    private static final String API_URL = "https://exercisedb.p.rapidapi.com/exercises";
    private static final String API_KEY = "29fa35a3famsh33afaf21dacf203p1d6c6bjsn353f5cd31aa7";
    private static final String API_HOST = "exercisedb.p.rapidapi.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        exerciseList = new ArrayList<>();
        exerciseAdapter = new ExerciseAdapter(exerciseList);
        Log.d("exerciseListinit", String.valueOf(exerciseList));
        recyclerView.setAdapter(exerciseAdapter);

        fetchExerciseData();

        spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);



        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // Get the selected filter option
                selectedFilter = adapterView.getItemAtPosition(position).toString();

                // Filter the exercise list based on the selected filter
                List<Exercise> filteredList = null;
                switch (selectedFilter) {
                    case "Body Part":
                        Toast.makeText(MainActivity.this, "Body Part", Toast.LENGTH_SHORT).show();
                        filteredList = filterByBodyPart();
                        break;
                    case "Target":
                        Toast.makeText(MainActivity.this, "Target", Toast.LENGTH_SHORT).show();
                        filteredList = filterByTarget();
                        break;
                    case "Equipment":
                        Toast.makeText(MainActivity.this, "Equipment", Toast.LENGTH_SHORT).show();
                        filteredList = filterByEquipment();
                        break;
                    default:
                        // "All Exercises" or unknown filter, show all exercises
                        filteredList = exerciseList;
                        break;
                }

                // Update the RecyclerView with the filtered list
                exerciseAdapter.setExerciseList(filteredList);
                exerciseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        // Other existing code ...
    }

    // Helper methods to filter exerciseList based on selected criteria
    private List<Exercise> filterByBodyPart() {
        List<Exercise> filteredList = new ArrayList<>();
        String selectedBodyPart = "waist"; // Replace this with the actual selected body part

        for (Exercise exercise : exerciseList) {
            if (exercise.getBodyPart().equalsIgnoreCase(selectedBodyPart)) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }

    private List<Exercise> filterByTarget() {
        List<Exercise> filteredList = new ArrayList<>();
        String selectedTarget = "abs"; // Replace this with the actual selected target

        for (Exercise exercise : exerciseList) {
            if (exercise.getTarget().equalsIgnoreCase(selectedTarget)) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }

    private List<Exercise> filterByEquipment() {
        List<Exercise> filteredList = new ArrayList<>();
        String selectedEquipment = "body weight"; // Replace this with the actual selected equipment

        for (Exercise exercise : exerciseList) {
            if (exercise.getEquipment().equalsIgnoreCase(selectedEquipment)) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }




    private void fetchExerciseData() {
        OkHttpClient client = new OkHttpClient();
        String apiUrl = API_URL;

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", API_HOST)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // Handle API request failure
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("msg",responseBody);

                    try {
                        // Check if the response is a JSON array
                        if (responseBody.startsWith("[")) {
                            JSONArray exerciseArray = new JSONArray(responseBody);
                            for (int i = 0; i < exerciseArray.length(); i++) {
                                JSONObject exerciseObject = exerciseArray.getJSONObject(i);

                                String bodyPart = exerciseObject.getString("bodyPart");
                                String equipment = exerciseObject.getString("equipment");
                                String gifUrl = exerciseObject.getString("gifUrl");
                                String id = exerciseObject.getString("id");
                                String name = exerciseObject.getString("name");
                                String target = exerciseObject.getString("target");

                                // Parse other exercise data fields

                                Exercise exercise = new Exercise(bodyPart,equipment,gifUrl,id,name,target);
                                exerciseList.add(exercise);
//                                Log.d("exerciseList0", String.valueOf(exerciseList));
//                                Log.d("exerciseList1", String.valueOf(exercise));
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    exerciseAdapter.notifyDataSetChanged();


//                                    Intent intent=new Intent(getApplicationContext(),MainActivity2.class);
//                                    startActivity(intent);

                                }
                            });
                        } else {
                            // Handle error: Unexpected JSON response format (not a JSON array)
                            Log.e("ExerciseApp", "Unexpected JSON response format: " + responseBody);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                        Log.e("ExerciseApp", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    // Handle API request failure (non-successful response)
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if((item.getItemId()==R.id.menu_myaccount)){
            Intent intent=new Intent(getApplicationContext(),MyAccount.class);
            startActivity(intent);
            Toast.makeText(this, "Your Acoount", Toast.LENGTH_SHORT).show();
        }
        else if((item.getItemId()==R.id.menu_calorieCount)){
            Intent intent=new Intent(getApplicationContext(),CalorieCount.class);
            startActivity(intent);
            Toast.makeText(this, "Check your Calorie count", Toast.LENGTH_SHORT).show();
        }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;

    }
}