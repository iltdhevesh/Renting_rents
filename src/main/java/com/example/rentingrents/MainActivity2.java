package com.example.rentingrents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity2 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Button btnRenter = findViewById(R.id.btnRenter);
        Button btnLandRent = findViewById(R.id.btnLandRent);


        // Example: Handling click on btnFoodWaste
        btnRenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add logic for handling Food Waste button click

                Intent intent = new Intent(MainActivity2.this, Renter.class);
                startActivity(intent);
            }
        });

        btnLandRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity2.this, LandRent.class);
                startActivity(intent);
            }
        });

    }

    // You can reuse the showToast method if needed
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        // Override onBackPressed to close the app when in MainActivity2
        finishAffinity();
    }
}

