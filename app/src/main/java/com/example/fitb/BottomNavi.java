package com.example.fitb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.fitb.databinding.ActivityBottomNaviBinding;
import com.google.firebase.auth.FirebaseAuth;

public class BottomNavi extends AppCompatActivity {

    ActivityBottomNaviBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_bottom_navi);
        binding=ActivityBottomNaviBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.navigation_dashboard:
                    replaceFragment(new PersonFragment());
                    break;

            }

            return true;
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if((item.getItemId()==R.id.menu_calorieCount)){
            Intent intent=new Intent(getApplicationContext(),CalorieCount.class);
            startActivity(intent);
            Toast.makeText(this, "Check your Calorie count", Toast.LENGTH_SHORT).show();
        }
        else if((item.getItemId()==R.id.menu_about)){
            Intent intent=new Intent(getApplicationContext(),About.class);
            startActivity(intent);
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
        }else if((item.getItemId()==R.id.menu_findFriends)){
            Intent intent=new Intent(getApplicationContext(),GymPartnersActivity.class);
            startActivity(intent);
            //Toast.makeText(this, "find Friends", Toast.LENGTH_SHORT).show();
        }else if((item.getItemId()==R.id.menu_logout)){
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
            //Toast.makeText(this, "Find Requests", Toast.LENGTH_SHORT).show();
        }else if((item.getItemId()==R.id.menu_editProile)){
        Intent intent=new Intent(getApplicationContext(),Profile.class);
        startActivity(intent);
        Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show();
    }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}