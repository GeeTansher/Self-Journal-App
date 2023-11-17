package com.example.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.journalApi;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnCreateAcc;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // FireStore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collection = db.collection("Users");

    private AutoCompleteTextView etEmail;
    private EditText etPass;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        btnLogin = findViewById(R.id.btnSignIn);
        btnCreateAcc = findViewById(R.id.btnCreateAccLogin);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progresslogin);

        firebaseAuth = FirebaseAuth.getInstance();


        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        CreateAccount.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmailPasswordUser(etEmail.getText().toString().trim(),
                        etPass.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(email)&&
        !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user !=null;
                            final String currUserId = user.getUid();

                            collection.whereEqualTo("userID", currUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value,
                                                            @Nullable FirebaseFirestoreException error) {

                                            assert value != null;
                                            if(!value.isEmpty()){
                                                progressBar.setVisibility(View.INVISIBLE);
                                                for (QueryDocumentSnapshot name : value) {
                                                    if (name.get("username") != null) {
                                                        journalApi journalApi = util.journalApi.getInstance();
                                                        journalApi.setUserId(currUserId);
                                                        journalApi.setUsername(name.getString("username"));

                                                        // go to list activity

                                                        startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                                    }
                                                }

                                            }
                                            else{
                                                Toast.makeText(LoginActivity.this, "Some error occurred...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
        }
    }
}