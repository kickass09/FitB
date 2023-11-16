package com.example.fitb;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

// ExerciseAdapter.java
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<Exercise> exerciseList;

    public ExerciseAdapter(List<Exercise> exerciseList) {
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);


        Glide.with(holder.itemView).load(exercise.getGifUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).into(holder.imgViewGif);
        holder.nameTextView.setText(exercise.getName());
        holder.descriptionTextView.setText(exercise.getBodyPart());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Created an Intent to launch the new activity
                Intent intent = new Intent(view.getContext(), ExerciseDetailsActivity.class);

                // Passed the exercise details as extras in the Intent
                intent.putExtra("bodyPart", exercise.getBodyPart());
                intent.putExtra("equipment", exercise.getEquipment());
                intent.putExtra("gifUrl", exercise.getGifUrl());
                intent.putExtra("id", exercise.getId());
                intent.putExtra("name", exercise.getName());
                intent.putExtra("target", exercise.getTarget());

                // Started the new activity
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }



    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView descriptionTextView;

        public ImageView imgViewGif;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imgViewGif=itemView.findViewById(R.id.imgViewGif);
        }
    }

    public void setExerciseList(List<Exercise> exerciseList) {
        this.exerciseList = exerciseList;
    }
}
