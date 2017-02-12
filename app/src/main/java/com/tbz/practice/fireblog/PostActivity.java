package com.tbz.practice.fireblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    ImageButton mImageButton;
    private EditText showTitleEt;
    private EditText showDescEt;
    private Button btnSubmit;

    private Uri mImageUri = null;

    private StorageReference mReference;
    private DatabaseReference mDatabase;

    private  static final int GALARY_REQUEST = 1;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        showTitleEt = (EditText) findViewById(R.id.titleEt);
        showDescEt = (EditText) findViewById(R.id.descriptionEt);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        mProgress = new ProgressDialog(this);

        mImageButton = (ImageButton) findViewById(R.id.btnImageSelect);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galaryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent,GALARY_REQUEST);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Posting on process...");
        mProgress.show();

        final String title_value = showTitleEt.getText().toString().trim();
        final String desc_value = showDescEt.getText().toString().trim();

        if(!TextUtils.isEmpty(title_value) && !TextUtils.isEmpty(desc_value) && mImageUri != null ){
            StorageReference filePath = mReference.child("Blog_Images").child(mImageUri.getLastPathSegment());

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = mDatabase.push();
                    newPost.child("title").setValue(title_value);
                    newPost.child("description").setValue(desc_value);
                    newPost.child("image").setValue(downloadUrl.toString());

                    mProgress.dismiss();
                    startActivity(new Intent(PostActivity.this,MainActivity.class));
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALARY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData();

            mImageButton.setImageURI(mImageUri);
        }

    }
}
