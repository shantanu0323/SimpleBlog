package shantanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 100;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private Button bRegister;
    private ImageButton bAddImage;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;

    private static final String TAG = "RegisterActivity";
    private Uri uri = null;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bRegister = (Button) findViewById(R.id.bLogin);
        bAddImage = (ImageButton) findViewById(R.id.bAddImage);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserImage();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });
    }

    private void startRegister() {
        final String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) &&
                !TextUtils.isEmpty(email) &&
                !TextUtils.isEmpty(password)) {
            progressDialog.setMessage("Registering...");
            progressDialog.show();

            Log.e(TAG, "startRegister: REGISTERING USER...");
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.e(TAG, "onComplete: REGISTERING USER SUCCESSFULL");
                                String userId = mAuth.getCurrentUser().getUid();

                                uploadImage();
                                DatabaseReference currentUser = mDatabase.child(userId);

                                currentUser.child("name").setValue(name);
                                currentUser.child("image").setValue("https://firebasestorage.googleapis.com/v0/b/simpleblog-a4d27.appspot.com/o/ProfilePics%2Fdefault_image.png?alt=media&token=a5f81ff5-75d2-4648-8772-6fe2dccc1f16");

                                progressDialog.dismiss();

                                Log.e(TAG, "onComplete: Redirecting to LoginActivity");
                                Intent intent = new Intent(getApplicationContext(), LoginActivity
                                        .class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: REGISTRATION FAILED due to : " + e.getMessage());
                }
            });
        }
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            bAddImage.setImageURI(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage() {
        Log.e(TAG, "uploadImage: FUNCTION STARTED");
        progressDialog.show();
        if (uri != null) {
            Log.e(TAG, "uploadImage: URI NOT NULL");
            StorageReference filePath = storageReference.child("ProfilePics").child(uri
                    .getLastPathSegment());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference newPost = mDatabase.child(mAuth.getCurrentUser().getUid());
                    newPost.child("image").setValue(downloadUrl.toString());
                    Log.e(TAG, "onSuccess: SETUP DONE ...");
                    progressDialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to finish setup...", Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "onFailure: FAILED TO FINISH SETUP");
                    progressDialog.dismiss();
                }
            });
        } else {
            Log.e(TAG, "uploadImage: URI NULL");
            DatabaseReference newPost = mDatabase.child(mAuth.getCurrentUser()
                    .getUid());
            newPost.child("image").setValue("https://firebasestorage.googleapis.com/v0/b/simpleblog-a4d27.appspot.com/o/ProfilePics%2Fimage%3A45846?alt=media&token=2f78a6d7-bbd9-44d1-905a-cf12e053be67");
            Log.e(TAG, "onSuccess: SETUP DONE ...");
            progressDialog.dismiss();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}

