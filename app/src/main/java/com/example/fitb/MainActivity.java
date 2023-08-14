package com.example.fitb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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