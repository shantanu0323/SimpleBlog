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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 100;
    private static final String TAG = "SetupActivity";

    private ImageButton bAddImage;
    private EditText etName;
    private Uri uri = null;
    private ProgressDialog progressDialog;
    private Button bFinishSetup;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        progressDialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        etName = (EditText) findViewById(R.id.etName);
        bAddImage = (ImageButton) findViewById(R.id.bAddImage);
        bFinishSetup = (Button) findViewById(R.id.bFinishSetup);

        bFinishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Finishing Setup...");
                Log.e(TAG, "onClick: FINISH BUTTON CLICKED");
                progressDialog.show();
                finishSetup();
            }
        });

        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserImage();
            }
        });
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void finishSetup() {
        Log.e(TAG, "finishSetup: FUNCTION STARTED");
        progressDialog.show();
        final String nameValue = etName.getText().toString();
        if (!TextUtils.isEmpty(nameValue)) {
            if (uri != null) {
                Log.e(TAG, "finishSetup: URI NOT NULL");
                StorageReference filePath = storageReference.child("ProfilePics").child(uri
                        .getLastPathSegment());
                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        DatabaseReference newPost = databaseReference.child(mAuth.getCurrentUser().getUid());
                        newPost.child("name").setValue(nameValue);
                        newPost.child("image").setValue(downloadUrl.toString());
                        Log.e(TAG, "onSuccess: SETUP DONE ...");
                        progressDialog.dismiss();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
                Log.e(TAG, "finishSetup: URI NULL");
                DatabaseReference newPost = databaseReference.child(mAuth.getCurrentUser().getUid());
                newPost.child("name").setValue(nameValue);
                newPost.child("image").setValue("https://firebasestorage.googleapis.com/v0/b/simpleblog-a4d27.appspot.com/o/ProfilePics%2Fdefault_image.png?alt=media&token=a5f81ff5-75d2-4648-8772-6fe2dccc1f16");
                Log.e(TAG, "onSuccess: SETUP DONE ...");
                progressDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                bAddImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}
