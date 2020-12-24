package com.evinyas.jkotekar.littlepos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText emailText;
    private EditText etPassword;

    ProgressDialog progressDialog;
    private static FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailText = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.etPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (firebaseDatabase == null) {
            FirebaseApp.initializeApp(this);
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    progressDialog.cancel();
                    updateUI(firebaseAuth.getCurrentUser());
                }

            }
        };

    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            progressDialog.cancel();
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void loginButton(View view) {
        String email = emailText.getText().toString();
        String pass = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(Login.this, "Enter Values", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Logging in");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.cancel();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else
                        Toast.makeText(Login.this, "Incorrect email or passowrd", Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    public void signUpButton(View view) {
        String email = emailText.getText().toString();
        String pass = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(Login.this, "Enter Values", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Signing up user");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.cancel();
                    if (!task.isSuccessful())
                        Toast.makeText(Login.this, "Error with signup" + task.getException(), Toast.LENGTH_LONG).show();
                    else {
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getCurrentUser().getUid() + "/email");
                        db.setValue(mAuth.getCurrentUser().getEmail());
                    }

                }
            });
        }
    }

    public void resetPassword(View view) {
        String email = emailText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Login.this, "Enter email address", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Sending email to reset password");
            progressDialog.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful())
                        Toast.makeText(Login.this, "Error resetting password", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(Login.this, "Reset instructions sent to your email addredd", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
