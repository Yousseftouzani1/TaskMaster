package com.example.devmob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;

    private MaterialButton loginButton;
    private TextView registerButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your layout with email/password/login UI

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerLink); // optional if you create a register activity
//  login as a user
        loginButton.setOnClickListener(v -> loginUser());
//  to the register activity
        registerButton.setOnClickListener(v -> {
            // Optional: Start registration activity
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
//  if the email or the password are empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "tous les champs sont necessaires !! ", Toast.LENGTH_LONG).show();
            return;
        }
//  login with email and password
//  mAuth is  the instance of FirebaseAuth for authentification using password and email
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            // save it in SharedPreferences, or use it immediately
                            Intent intent = new Intent(MainActivity.this, TaskListActivity.class);
                            intent.putExtra("USER_UID", uid);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        Log.e("FIREBASE_LOGIN", "Erreur : ", task.getException());
                        Toast.makeText(MainActivity.this, "Échec de la connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
