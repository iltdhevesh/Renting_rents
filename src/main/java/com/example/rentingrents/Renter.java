package com.example.rentingrents;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Renter extends BaseActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int PICK_COORDINATES_REQUEST = 2;

    private ArrayList<String> imageUrls = new ArrayList<>(4);
    private DataManager dataManager;
    private Button btnUploadImages, btnChooseCoordinates;
    private EditText etName, etNumber, etAddress, etAreaName;
    private String storedName;
    private String storedNumber;
    private EditText etDescription;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    private boolean isNavigationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter);
        dataManager = new DataManager(this);
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnChooseCoordinates = findViewById(R.id.btnChooseCoordinates);
        etName = findViewById(R.id.etName);
        etNumber = findViewById(R.id.etNumber);
        etAddress = findViewById(R.id.etAddress);
        etAreaName = findViewById(R.id.etAreaName);
        etDescription = findViewById(R.id.etDescription);
        storedName = dataManager.getUserName();
        storedNumber = dataManager.getUserPhoneNumber();
        etName.setText(storedName);
        etNumber.setText(storedNumber);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("images");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait while the images are uploading");
        progressDialog.setCancelable(false);
        btnUploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnChooseCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if all mandatory fields are filled
                if (areDetailsFilled() && areImagesSelected()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Renter.this);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Please make sure you are on the Land location");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String setIdentifier = String.valueOf(System.currentTimeMillis());
                            uploadImagesAndNavigate(setIdentifier);
                        }
                    });
                    builder.setNegativeButton("Cancel", null); // Optional: Handle cancel action
                    builder.setCancelable(false);
                    builder.show();
                } else {
                    Toast.makeText(Renter.this, "Please fill all the mandatory details and select images", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean areDetailsFilled() {
        return !etName.getText().toString().isEmpty() &&
                !etNumber.getText().toString().isEmpty() &&
                !etAddress.getText().toString().isEmpty() &&
                !etAreaName.getText().toString().isEmpty() &&
                !etDescription.getText().toString().isEmpty();
    }

    private boolean areImagesSelected() {
        return !imageUrls.isEmpty();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    private void uploadImagesAndNavigate(String setIdentifier) {
        progressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            if (imageUrl == null) continue;

            Uri imageUri = Uri.parse(imageUrl);

            // Create a unique path for each image using the setIdentifier
            StorageReference imageRef = storageReference.child(setIdentifier).child("image_" + i + ".jpg");

            int finalIndex = i;

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.set(finalIndex, uri.toString());

                            // Add the image URL to the database
                            databaseReference.child(setIdentifier).child("images").child(String.valueOf(finalIndex)).setValue(uri.toString());

                            if (areAllImagesUploaded() && !isNavigationStarted) {
                                progressDialog.dismiss();
                                isNavigationStarted = true;
                                navigateToMapsActivity(setIdentifier);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(Renter.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private boolean areAllImagesUploaded() {
        for (String url : imageUrls) {
            if (url == null) return false;
        }
        return true;
    }

    private void navigateToMapsActivity(String setIdentifier) {
        Intent intent = new Intent(Renter.this, MapsActivity.class);
        intent.putExtra("name", etName.getText().toString());
        intent.putExtra("number", etNumber.getText().toString());
        intent.putExtra("address", etAddress.getText().toString());
        intent.putExtra("areaName", etAreaName.getText().toString());
        intent.putExtra("description", etDescription.getText().toString());
        intent.putExtra("uniqueIdentifier", setIdentifier);
        startActivityForResult(intent, PICK_COORDINATES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGES_REQUEST && data != null) {
                handleImageSelection(data);
            }
        }
    }


    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images selected
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri imageUri = clipData.getItemAt(i).getUri();
                // Handle the Uri as needed, e.g., convert it to a String and store in selectedImageUrls array
                imageUrls.add(imageUri.toString());
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            // Handle the Uri as needed, e.g., convert it to a String and store in selectedImageUrls array
            imageUrls.add(imageUri.toString());
        }


    }
}
