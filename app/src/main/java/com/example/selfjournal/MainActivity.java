package com.example.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    private Button btngetStarted;
    private TextView tvGratefulText;
    String text = "  What are you grateful about today?     ";
    int i=0,j=text.length()-1;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        btngetStarted = findViewById(R.id.btnGetStarted);
        tvGratefulText = findViewById(R.id.tvGratefulText);
        forward(text);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currUser = firebaseAuth.getCurrentUser();
                if(currUser!=null){
                    String currUserId = currUser.getUid();

                    collectionReference
                            .whereEqualTo("userID", currUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(error!=null){
                                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    if(!value.isEmpty()){
                                        for(QueryDocumentSnapshot snapshot:value){
                                            journalApi journalApi = util.journalApi.getInstance();
                                            journalApi.setUserId(snapshot.getString("userID"));
                                            journalApi.setUsername(snapshot.getString("username"));

                                            startActivity(new Intent(MainActivity.this,
                                                    JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }
                else{

                }
            }
        };

        btngetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                        LoginActivity.class));
                finish();
            }
        });
    }

    private void forward(String str) {
        if(i<str.length()){
            String string=str.substring(0,i);
            tvGratefulText.setText(string);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    i++;
                    forward(str);
                }
            },50);
        }
        else{
            j=text.length()-1;
            backward(text);
        }
    }

    private void backward(String str){
        if(j>=0){
            String string=str.substring(0,j);
            tvGratefulText.setText(string);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    j--;
                    backward(str);
                }
            },30);
        }
        else{
            i=0;
            forward(text);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}