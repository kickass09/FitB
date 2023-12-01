package com.example.fitb;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

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
//    private static final String API_KEY = "29fa35a3famsh33afaf21dacf203p1d6c6bjsn353f5cd31aa8";
private static final String API_KEY = "29fa35a3famsh33afaf21dacf203p1d6c6bjsn353f5cd31aa7";
    private static final String API_HOST = "exercisedb.p.rapidapi.com";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        spinnerSubFilter = view.findViewById(R.id.spinnerSubFilter);
        subFilterOptions = new ArrayList<>();
        subFilterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subFilterOptions);
        subFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubFilter.setAdapter(subFilterAdapter);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        exerciseList = new ArrayList<>();
        exerciseAdapter = new ExerciseAdapter(exerciseList);
        recyclerView.setAdapter(exerciseAdapter);

        fetchExerciseData();

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
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

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
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
                List<Exercise> filteredList = applySubFilter(selectedFilter, selectedSubFilter);
                exerciseAdapter.setExerciseList(filteredList);
                exerciseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        return view;
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

                    try {
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

                                Exercise exercise = new Exercise(bodyPart, equipment, gifUrl, id, name, target);
                                exerciseList.add(exercise);
                            }

                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    exerciseAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            Log.e("ExerciseApp", "Unexpected JSON response format: " + responseBody);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ExerciseApp", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    // Handle API request failure (non-successful response)
                }
            }
        });
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
}