package shantanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewPost extends AppCompatActivity {

    private ImageButton bAddImage;
    private TextView tvImageLabel;
    private EditText etTitle;
    private EditText etDesc;
    private Button bSubmit;

    private static final int GALLERY_REQUEST = 1;
    private Uri uri = null;
    private StorageReference storageReference = null;
    private DatabaseReference databaseReference = null;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        init();

        progressDialog.setMessage("Posting Blog...");
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blogs");

        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST);

            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        progressDialog.show();
        final String titleValue = etTitle.getText().toString();
        final String descValue = etDesc.getText().toString();
        if (!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descValue) && uri != null) {
            StorageReference filePath = storageReference.child("BlogImages").child(uri
                    .getLastPathSegment());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(NewPost.this, "Blog is posted Successfully!!!", Toast
                            .LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference newPost = databaseReference.push();
                    newPost.child("title").setValue(titleValue);
                    newPost.child("desc").setValue(descValue);
                    newPost.child("image").setValue(downloadUrl.toString());

                    progressDialog.dismiss();
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewPost.this, "Failed to Post Blog...", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            bAddImage.setImageURI(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
