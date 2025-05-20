package com.example.devmob;

import android.annotation.SuppressLint;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.GoogleApiClient;  // if you use it
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.services.calendar.CalendarScopes; // <-- This one is important

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;

    private MaterialButton loginButton;
    private TextView registerButton;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
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
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CalendarScopes.CALENDAR))

                .requestIdToken(getString(R.string.default_web_client_id)) // This comes from google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

// Add Google Sign-In button logic (see below)
        findViewById(R.id.googleSignInButton).setOnClickListener(v -> signInWithGoogle());

    }
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Échec de connexion Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Connexion Google réussie", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, TaskListActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Échec de l'authentification Firebase", Toast.LENGTH_SHORT).show();
                    }
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
                        Toast.makeText(MainActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        // Go to next activity, like TaskListActivity
                        startActivity(new Intent(MainActivity.this, TaskListActivity.class));
                        finish();
                    } else {
                        Log.e("FIREBASE_LOGIN", "Erreur : ", task.getException());
                        Toast.makeText(MainActivity.this, "Échec de la connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
