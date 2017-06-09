package shantanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewPost extends AppCompatActivity {

    private static final String TAG = "NewPost";

    private ImageButton bAddImage;
    private TextView tvImageLabel;
    private EditText etTitle;
    private EditText etDesc;
    private Button bSubmit;

    private static final int GALLERY_REQUEST = 200;
    private Uri uri = null;
    private ProgressDialog progressDialog = null;

    private StorageReference storageReference = null;
    private DatabaseReference databaseReference = null;
    private DatabaseReference databaseUsers = null;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        init();

        progressDialog.setMessage("Posting Blog...");

        mAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blogs");
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth
                .getCurrentUser().getUid());

        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickImageFromGallery();

            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void pickImageFromGallery() {

        /*
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
        */

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    private void startPosting() {
        final String titleValue = etTitle.getText().toString();
        final String descValue = etDesc.getText().toString();
        final String timeValue = getCurrentTime();

        if (!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descValue) && uri != null) {
            progressDialog.show();

            StorageReference filePath = storageReference.child("BlogImages").child(uri
                    .getLastPathSegment());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(NewPost.this, "Blog is posted Successfully!!!", Toast
                            .LENGTH_SHORT).show();
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newPost = databaseReference.push();

                    Log.e(TAG, "onSuccess: Starts adding");

                    databaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newPost.child("title").setValue(titleValue);
                            newPost.child("desc").setValue(descValue);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("time").setValue(timeValue);
                            newPost.child("uid").setValue(mAuth.getCurrentUser().getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name")
                                    .getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.e(TAG, "onSuccess: Redirecting to MainActivity");
                                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewPost.this, "Failed to Post Blog...", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: FAILED TO POST BLOG due to : " + e.getMessage());
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Please fill in the fields properly", Toast
                    .LENGTH_SHORT).show();
            Log.e(TAG, "startPosting: Null values");
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            Log.e(TAG, "onActivityResult: Image Received successfully : " + (uri != null));
            InputStream inputStream = null;
            try {
                inputStream = getApplicationContext().getContentResolver()
                        .openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bAddImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            bAddImage.invalidate();
//            bAddImage.setImageURI(uri);
        } else {
            Log.e(TAG, "onActivityResult: IMAGE RETRIEVING FAILED");
        }
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        Log.e("Current time => ", " " + c.getTime());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d");
        String date = dateFormatter.format(c.getTime());

        if(date.endsWith("1") && !date.endsWith("11"))
            dateFormatter = new SimpleDateFormat("d'st' MMMM',' yyyy'\n' h:mm a");
        else if(date.endsWith("2") && !date.endsWith("12"))
            dateFormatter = new SimpleDateFormat("d'nd' MMMM',' yyyy'\n' h:mm a");
        else if(date.endsWith("3") && !date.endsWith("13"))
            dateFormatter = new SimpleDateFormat("d'rd' MMMM',' yyyy'\n' h:mm a");
        else
            dateFormatter = new SimpleDateFormat("d'th' MMMM',' yyyy'\n' h:mm a");

        String formattedDate = dateFormatter.format(c.getTime());
        Log.e("Current time => ", formattedDate);
        return formattedDate;
    }

    private void init() {
        bAddImage = (ImageButton) findViewById(R.id.bAddImage);
        tvImageLabel = (TextView) findViewById(R.id.tvImageLabel);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etDesc = (EditText) findViewById(R.id.etDesc);
        bSubmit = (Button) findViewById(R.id.bSubmit);
        uri = null;
        progressDialog = new ProgressDialog(this);
    }

}
