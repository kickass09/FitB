package com.example.fitb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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

    private static final int REQUEST_SCHEDULE_ALARM_PERMISSION = 123;

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;

    private Spinner spinnerSubFilter;
    private ArrayAdapter<String> subFilterAdapter;
    private List<String> subFilterOptions;

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

        spinnerSubFilter = findViewById(R.id.spinnerSubFilter);
        subFilterOptions = new ArrayList<>();
        subFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subFilterOptions);
        subFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubFilter.setAdapter(subFilterAdapter);

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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "FitB";
            CharSequence channelName = "Reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        scheduleNotification();



        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // Get the selected filter option
                selectedFilter = adapterView.getItemAtPosition(position).toString();

                updateSubFilterOptions(selectedFilter);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });


        spinnerSubFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSubFilter = adapterView.getItemAtPosition(position).toString();
                String selectedFilter = spinnerFilter.getSelectedItem().toString();
                // Apply sub-filtering logic and update the RecyclerView
                List<Exercise> filteredList = applySubFilter(selectedFilter, selectedSubFilter);
                exerciseAdapter.setExerciseList(filteredList);
                exerciseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SCHEDULE_ALARM_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotification();
            } else {
                // Handle the case where permission was denied
            }
        }
    }

    private void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, YourNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the time you want the notification to be triggered
        long triggerTimeMillis = System.currentTimeMillis() + 5000;
        long intervalMillis = 1*1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, intervalMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, intervalMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTimeMillis, intervalMillis, pendingIntent);
        }
    }

    // Method to update sub-filter options based on the selected primary filter
    private void updateSubFilterOptions(String primaryFilter) {
        subFilterOptions.clear();

        if (primaryFilter.equals("Body Part")) {
            // Add body part options to sub-filter
            subFilterOptions.add("lower arms");

            subFilterOptions.add("lower legs");
            subFilterOptions.add("neck");
            subFilterOptions.add("shoulders");
            subFilterOptions.add("upper arms");
            subFilterOptions.add("upper legs");

            subFilterOptions.add("waist");
            subFilterOptions.add("back");
            subFilterOptions.add("cardio");
            subFilterOptions.add("chest");
        } else if (primaryFilter.equals("Target")) {
            // Add target options to sub-filter
            subFilterOptions.add("abductors");
            subFilterOptions.add("adductors");
            subFilterOptions.add("biceps");
            subFilterOptions.add("calves");
            subFilterOptions.add("cardiovascular system");
            subFilterOptions.add("delts");
            subFilterOptions.add("forearms");
            subFilterOptions.add("glutes");
            subFilterOptions.add("hamstrings");
            subFilterOptions.add("lats");
            subFilterOptions.add("levator scapulae");
            subFilterOptions.add("pectorals");
            subFilterOptions.add("quads");
            subFilterOptions.add("serratus anterior");
            subFilterOptions.add("spine");
            subFilterOptions.add("traps");


            subFilterOptions.add("abs");
            subFilterOptions.add("triceps");
            subFilterOptions.add("upper back");

        } else if (primaryFilter.equals("Equipment")) {
            // Add equipment options to sub-filter
            subFilterOptions.add("assisted");
            subFilterOptions.add("band");
            subFilterOptions.add("body weight");
            subFilterOptions.add("bosu ball");
            subFilterOptions.add("cable");
            subFilterOptions.add("dumbbell");
            subFilterOptions.add("elliptical machine");
            subFilterOptions.add("ez barbell");
            subFilterOptions.add("hammer");
            subFilterOptions.add("kettlebell");
            subFilterOptions.add("leverage machine");
            subFilterOptions.add("medicine ball");
            subFilterOptions.add("olympic barbell");
            subFilterOptions.add("resistance band");
            subFilterOptions.add("roller");
            subFilterOptions.add("skierg machine");
            subFilterOptions.add("sled machine");
            subFilterOptions.add("smith machine");
            subFilterOptions.add("stability ball");
            subFilterOptions.add("stationary bike");
            subFilterOptions.add("stepmill machine");
            subFilterOptions.add("barbell");
            subFilterOptions.add("rope");
            subFilterOptions.add("tire");

        }

        subFilterAdapter.notifyDataSetChanged();
    }

    // Method to apply sub-filtering logic and return the filtered list
    private List<Exercise> applySubFilter(String primaryFilter, String subFilter) {
        List<Exercise> filteredList = new ArrayList<>();

        // Apply filtering logic based on the selected primary filter and sub-filter
        if (primaryFilter.equals("Body Part")) {
            filteredList = filterByBodyPart(subFilter);
        } else if (primaryFilter.equals("Target")) {
            filteredList = filterByTarget(subFilter);
        } else if (primaryFilter.equals("Equipment")) {
            filteredList = filterByEquipment(subFilter);
        }
        updateSubFilterOptions(selectedFilter);
        return filteredList;
    }

    // Helper methods to filter exerciseList based on selected criteria
    private List<Exercise> filterByBodyPart(String subFilter) {
        List<Exercise> filteredList = new ArrayList<>();

        for (Exercise exercise : exerciseList) {
            if (exercise.getBodyPart().equalsIgnoreCase(subFilter)) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }

    private List<Exercise> filterByTarget(String subFilter) {
        List<Exercise> filteredList = new ArrayList<>();

        for (Exercise exercise : exerciseList) {
            if (exercise.getTarget().equalsIgnoreCase(subFilter)) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }

    private List<Exercise> filterByEquipment(String subFilter) {
        List<Exercise> filteredList = new ArrayList<>();

        for (Exercise exercise : exerciseList) {
            if (exercise.getEquipment().equalsIgnoreCase(subFilter)) {
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
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    exerciseAdapter.notifyDataSetChanged();


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
        else if((item.getItemId()==R.id.menu_about)){
            Intent intent=new Intent(getApplicationContext(),About.class);
            startActivity(intent);
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
        }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;

    }
}