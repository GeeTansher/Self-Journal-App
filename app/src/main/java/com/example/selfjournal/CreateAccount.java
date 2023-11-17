package com.example.selfjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.journalApi;

public class CreateAccount extends AppCompatActivity {
    private Button btnCreateAcc;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // FireStore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    private EditText etEmailAcc, etPassAcc, etUsername;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        btnCreateAcc = findViewById(R.id.btnCreateAcc);
        etEmailAcc = findViewById(R.id.etEmailAcc);
        etPassAcc = findViewById(R.id.etPasswordAcc);
        etUsername = findViewById(R.id.etUsername);
        progressBar = findViewById(R.id.acc_progresslogin);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser!=null){
                    // user Logged in
                }
                else{
                    // no user yet
                }
            }
        };

        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailAcc.getText().toString().trim();
                String password = etPassAcc.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                if(!TextUtils.isEmpty(email)
                        && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(username)) {
                    createUserEmailAcc(email, password, username);
                }
                else{
                    Toast.makeText(CreateAccount.this, "Empty String Not Allowed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserEmailAcc(String email, String password, String username){
        if(!TextUtils.isEmpty(email)
            && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // we can go to addJournal
                                currentUser = firebaseAuth.getCurrentUser();
                                String currUserId = firebaseAuth.getUid();
                                Map<String,String> userObj = new HashMap<>();
                                userObj.put("userID", currUserId);
                                userObj.put("username", username);

                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if(task.getResult().exists()){
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name = task.getResult()
                                                                            .getString("username");

                                                                    journalApi journalApi = util.journalApi.getInstance();
                                                                    journalApi.setUserId(currUserId);
                                                                    journalApi.setUsername(username);

                                                                    Intent intent = new Intent(CreateAccount.this,
                                                                            PostJournalActivity.class);
//                                                                    intent.putExtra("username", name);
//                                                                    intent.putExtra("userId", currUserId);
                                                                    startActivity(intent);
                                                                }
                                                                else{

                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else{
                                // something went wrong
                                Toast.makeText(CreateAccount.this, "Creating account failed. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}