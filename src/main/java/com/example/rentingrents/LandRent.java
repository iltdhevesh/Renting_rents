package com.example.rentingrents;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LandRent extends BaseActivity {

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landrent);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading the lands...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // Find the LinearLayout to dynamically add buttons
        LinearLayout linearLayout = findViewById(R.id.yourLinearLayoutId);

        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("buttons");

        // Attach a listener to read the data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    String link = entrySnapshot.getValue(String.class);
                    String label = entrySnapshot.getKey();

                    // Construct the Firebase Storage download URL based on the filename
                    String storageUrl = "https://firebasestorage.googleapis.com/v0/b/rentingrents.appspot.com/o/" + link + "?alt=media";

                    // Create buttons dynamically with associated ID
                    createButton(linearLayout, label, storageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void createButton(LinearLayout linearLayout, String label, final String link) {
        String[] labelParts = label.split(" - ");
        String label1 = labelParts[0]+"-"+labelParts[1];

        Button button = new Button(this);

        // Generate a unique ID for the button
        int buttonId = View.generateViewId();

        // Set the ID for the button
        button.setId(buttonId);

        // Set onClickListener for the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle button click based on the button ID
                handleButtonClick(buttonId, label, link);
            }
        });
        button.setText(label1);
        linearLayout.addView(button);
    }

    private void handleButtonClick(int buttonId, String label, String link) {
        // Handle button click based on the button ID
        Toast.makeText(this, "Button Clicked with ID: " + buttonId, Toast.LENGTH_SHORT).show();
        Log.d("LandRent", "Button Clicked with ID: " + buttonId);


        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select option").setItems(R.array.land_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String[] labelParts = label.split(" - ");
                    String setIdentifier = labelParts[2];
                    switch (i) {
                        case 0:
                            SendWhatsApp(label);
                            break;
                        case 1:
                            openImageWithSetIdentifier("image_0.jpg", setIdentifier);
                            break;
                        case 2:
                            openImageWithSetIdentifier("image_1.jpg", setIdentifier);
                            break;
                        case 3:
                            openImageWithSetIdentifier("image_2.jpg", setIdentifier);
                            break;
                        case 4:
                            openImageWithSetIdentifier("image_3.jpg", setIdentifier);
                            break;
                        default:
                            Log.e("LandRent", "Unhandled button label: " + label);
                            break;

                    }


            }
        });
                builder.create().show();

    }
    private void SendWhatsApp(String message) {
        String encodedMessage = Uri.encode(message);

        Uri uri = Uri.parse("https://wa.me/918015153603?text=" + "Details needed, from Renting Rents\nLand id: " + encodedMessage);

        // Create an Intent to open the WhatsApp application with the pre-composed message
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openImageWithSetIdentifier(String link, String setIdentifier) {
        // Construct the URL with the set identifier
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/rentingrents.appspot.com/o/"
                + setIdentifier + "%2F" + link + "?alt=media&token=05b395d4-3e95-4cb8-ae99-c52469567b67";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        startActivity(browserIntent);
    }



    @Override
    public void onBackPressed() {
        // Override the back button behavior to go back to MainActivity2
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity
    }
}
