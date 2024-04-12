package com.example.rentingrents;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhoneNumber, editTextPassword;
    private Button btnLogin, btnRegister;
    private TextView tvRg;
    private LinearLayout layoutLoggedIn;
    private DataManager dataManager;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataManager = new DataManager(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        editTextName = findViewById(R.id.editTextName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvRg = findViewById(R.id.tvDontHaveAccount);
        if (mAuth.getCurrentUser() != null) {
            // User is already signed in, navigate to the home screen or any other screen
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            // Optional: Finish the current activity to prevent going back to the login screen
        }
        // Set onClickListeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the input values
                String name = editTextName.getText().toString().trim();
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                dataManager.saveUserData(name, phoneNumber);

                // Validate inputs
                if (name.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
                    showToast("Please fill in all the fields.");
                } else {
                    // If all fields are filled, proceed with login
                    loginUser(name, phoneNumber, password);
                }
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the registration page
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
    public void openWhatsApp(View view) {
        String phoneNumber = "8015153603";

        Uri uri = Uri.parse("smsto:" + phoneNumber);

        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);

        intent.setPackage("com.whatsapp");

        startActivity(intent);
    }

    public void sendMail(View view) {
        // Implement the logic to send an email
        String email = "iltdhevesh@gmail.com";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(intent);
    }



    public void callPhoneNumber(View view) {
        // Implement the logic to initiate a phone call
        String phoneNumber = "tel:" + "8015153603";

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(phoneNumber));
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loginUser(String name, String phoneNumber, String password) {
        mAuth.signInWithEmailAndPassword(phoneNumber + "@example.com", password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            showToast("Login Successful!");

                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            // If sign-in fails, display a message to the user.
                            showToast("Authentication failed. Please check your credentials.");
                        }
                    }
                });
    }

}