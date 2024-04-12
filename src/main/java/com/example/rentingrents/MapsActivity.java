package com.example.rentingrents;



import android.Manifest;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;


import java.io.FileOutputStream;
import java.io.IOException;



import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


import com.example.rentingrents.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int REQUEST_LAND_REGISTERED = 1;
    private static final int REQUEST_SMS_PERMISSION = 123;
    private StorageReference storageReference;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private PlacesClient placesClient;
    private LatLng lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button bookButton = findViewById(R.id.bookButton);

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastKnownLocation != null) {
                    // Get the location coordinates

                    double latitude = lastKnownLocation.latitude;
                    double longitude = lastKnownLocation.longitude;

                    // Retrieve information from the previous activity
                    String storedName = getIntent().getStringExtra("name");
                    String storedNumber = getIntent().getStringExtra("number");
                    String setIdentifier = getIntent().getStringExtra("uniqueIdentifier");
                    String storedAddress = getIntent().getStringExtra("address");
                    String storedAreaName = getIntent().getStringExtra("areaName");
                    String storedDescription = getIntent().getStringExtra("description");
                    uploadPasscodeToDatabase(setIdentifier,storedNumber);
                    // Display a message with payment and passcode entry options
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setMessage("Pay Rs 10, you may receive a passcode. Enter it to register your land.")
                            .setPositiveButton("Pay Rs 10", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Handle payment, e.g., open Google Pay with the amount of 10
                                    // This code should be replaced with your actual payment implementation

                                    String upiId = "iltdhevesh@okicici";

                                    // Replace "Test Transaction" with the description or note for the transaction
                                    String transactionNote = "Renter";

                                    // Replace "1" with the actual amount you want to send
                                    int amount = 10;

                                    // Create a UPI payment URI
                                    Uri uri = Uri.parse("upi://pay?pa=" + upiId + "&pn=TestMerchant&mc=123456&tid=123456" +
                                            "&tr=12345678&tn=" + transactionNote + "&am=" + amount + "&cu=INR");

                                    // Create an Intent to launch Google Pay
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    intent.setPackage("com.google.android.apps.nbu.paisa.user");

                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });

                    // Add a text input field for entering the passcode
                    final EditText passcodeInput = new EditText(MapsActivity.this);
                    passcodeInput.setHint("Enter passcode");
                    builder.setView(passcodeInput);

                    // Set up the dialog and show it
                    AlertDialog dialog = builder.create();

                    // Add the "Verify" button
                    // Add the "Verify" button
                    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Verify", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Get the entered passcode
                            String enteredPasscode = passcodeInput.getText().toString().trim();

                            // Check if the passcode is valid
                            if (isValidPasscode(enteredPasscode, setIdentifier)) {
                                // Passcode is valid, proceed with creating PDF and uploading
                                Toast.makeText(MapsActivity.this, "Passcode is valid", Toast.LENGTH_SHORT).show();

                                // Create PDF document
                                String fileName = "location_info.pdf";
                                createPdfDocument(fileName, lastKnownLocation.latitude, lastKnownLocation.longitude, storedName, storedNumber, storedAddress, storedAreaName, storedDescription);

                                // Upload PDF file
                                uploadWordFileToStorage(fileName);
                                startLandRentActivity(storedName, storedAreaName, fileName);
                                // You can also perform additional actions here if needed
                                uploadTextFileLinkToDatabase(storedName, storedAreaName, fileName, setIdentifier);
                                dialog.dismiss(); // Dismiss the dialog if the passcode is valid
                            } else {
                                // Passcode is invalid, display an error message
                                Toast.makeText(MapsActivity.this, "Invalid passcode", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                } else {
                    // Handle the case when lastKnownLocation is not available
                    Toast.makeText(MapsActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Places.initialize(getApplicationContext(), "AIzaSyDstfeKPv45OX0DZjJnzMFJBVjqtkklPVw");
        placesClient = Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    private boolean isValidPasscode(String enteredPasscode, String uniqueIdentifier) {
        // Compare the entered passcode with the unique identifier
        return enteredPasscode.equals(uniqueIdentifier);
    }

    private void uploadPasscodeToDatabase(String setIdentifier, String contactNumber) {

        sendSMS();
        String Label = setIdentifier+"-"+contactNumber;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("passcode");
        databaseReference.child(Label).setValue(1);

    }
    private void sendSMS() {
        // Check for SMS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            // SMS permission granted, send SMS
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("+918015153603", null, "Passcode of Renting Rents", null, null);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
    private void createPdfDocument(String fileName, double latitude, double longitude, String name, String number, String address, String areaName,String Description) {
        try {

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new FileOutputStream(getFilesDir() + "/" + fileName)));
            Document document = new Document(pdfDoc);

            String locationText = "http://maps.google.com/maps?q=" + latitude + "," + longitude;
            document.add(new Paragraph("Location: ").add(new Link("Click here for location", PdfAction.createURI(locationText)).setUnderline().setFontColor(ColorConstants.BLUE)));
            document.add(new Paragraph("Name: "+ name));
            document.add(new Paragraph("Number: ").add(new Link(number, PdfAction.createURI("tel:" + number)).setFontColor(ColorConstants.BLUE)));
            document.add(new Paragraph("Address: " + address));
            document.add(new Paragraph("Area Name: " + areaName));
            document.add(new Paragraph("Land Description " + Description));
            document.close();
            Toast.makeText(this, "Location information saved and uploaded as a PDF document", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating or uploading PDF document", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadWordFileToStorage(String fileName) {
        // Get the local file URI
        String filePath = getFilesDir() + "/" + fileName;
        Uri fileUri = Uri.parse("file://" + filePath);

        // Define Firebase Storage references
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(fileName);

        // Display a progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering your land...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Upload the file to Firebase Storage
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    // Dismiss the progress dialog
                    progressDialog.dismiss();

                    // Display a success Toast
                    Toast.makeText(MapsActivity.this, "Location information uploaded successfully", Toast.LENGTH_SHORT).show();

                    // Additional logic...
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    progressDialog.dismiss();
                    Toast.makeText(MapsActivity.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LAND_REGISTERED && resultCode == RESULT_OK) {
            // The result is OK, meaning the user has finished with LandRegisteredActivity
            // You can handle any additional logic here if needed

            // Finish the current activity (MapsActivity)
            finish();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        // Enable the "My Location" layer on the map
        mMap.setMyLocationEnabled(true);

        // Initialize location callback to update the camera when the location changes
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    lastKnownLocation = new LatLng(
                            locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()
                    );

                    // Add a marker at the current location and move the camera
                    mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("My Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation));

                    // Stop location updates after the first update
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        // Request location updates
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    private void startLandRentActivity(String name, String areaName, String fileName) {
        Intent landRentIntent = new Intent(MapsActivity.this, LandRent.class);
        landRentIntent.putExtra("name", name);
        landRentIntent.putExtra("areaName", areaName);
        landRentIntent.putExtra("textFileLink", fileName);
        startActivity(landRentIntent);
    }
    private void uploadTextFileLinkToDatabase(String storedName,String storedAreaName,String fileName,String setIdentifier) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("buttons");

        String buttonLabel = storedName + " - " + storedAreaName+ " - " + setIdentifier;

        databaseReference.child(buttonLabel).setValue(fileName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Handle the result of the location permission request
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, call onMapReady again to initialize the map
                onMapReady(mMap);
            }
        }
    }
}