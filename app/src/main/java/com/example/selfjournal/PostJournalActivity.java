package com.example.selfjournal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.journalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private static final String TAG = "TAG";
    private Button btnSave;
    private ProgressBar progressBar;
    private ImageView btnAddPhoto;
    private EditText etTitle, etThoughts;
    private TextView tvCurrUser;
    private ImageView imageView;
    private Uri imageUri;
    ActivityResultLauncher<String> getImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    // uri image
                    if (result != null) {
                        imageUri = result;
                        imageView.setImageURI(imageUri);
                    }
                }
            }
    );
    private String currUsername, currUserId;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private final CollectionReference collectionReference = db.collection("Journal");


//    ActivityResultLauncher<Intent> getGalleryImage = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    Log.d(TAG, "onActivityResult: ");
//                }
//            }
//    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.postProgressBar);
        btnSave = findViewById(R.id.btnPostSaveJournal);
        btnAddPhoto = findViewById(R.id.btnPostCamera);
        etTitle = findViewById(R.id.etPostTitle);
        etThoughts = findViewById(R.id.etPostThoughts);
        tvCurrUser = findViewById(R.id.tvPostUsername);
        imageView = findViewById(R.id.postImageView);

        btnSave.setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);

        if (journalApi.getInstance() != null) {
            currUsername = journalApi.getInstance().getUsername();
            currUserId = journalApi.getInstance().getUserId();

            tvCurrUser.setText(currUsername);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // user present
                }
            }
        };

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnPostCamera) {
            // get image from gallery
            getImage.launch("image/*");
//            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            galleryIntent.setType("image/*");
//            getGalleryImage.launch(galleryIntent);

        } else if (v.getId() == R.id.btnPostSaveJournal) {
            saveJournal();
        }

    }


    private void saveJournal() {
        String title = etTitle.getText().toString().trim();
        String thoughts = etThoughts.getText().toString().trim();

        if (!TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(thoughts) &&
                imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);

            StorageReference filePath = storageReference   // .../journal_images/my_image_5675.jpeg
                    .child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds());
            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Todo: create a journal object
                                            Journal journal = new Journal();
                                            journal.setTitle(title);
                                            journal.setThoughts(thoughts);
                                            journal.setImageUrl(uri.toString());
                                            journal.setTimeAdded(new Timestamp(new Date()));
                                            journal.setUserId(currUserId);
                                            journal.setUserName(currUsername);

                                            // Todo: invoke our journal reference and save journal

                                            collectionReference.add(journal)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            progressBar.setVisibility(View.INVISIBLE);

                                                            startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(PostJournalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(PostJournalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}