package com.example.rplicenseplaterecognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class SecondActivity2 extends AppCompatActivity {


    ImageView mImageView;
    ImageView mCamera;
    ImageView mGallery;
    ImageView mTextBox;
    ImageView mYesBox;
    ImageView mNoBox;

    Button mChooseBtn;
    Button mCameraBtn;
    Button mYes;
    Button mNo;
    String ImagePath;
    EditText mDetectedtxtview;
    TextView mLicensePlate;

    Group mFirstPage;
    Group mSecondPage;
    Group mDetect;

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int IMAGE_CAPTURE_CODE = 1002;
    private DatabaseReference mDatabase;

    //pick from gallery
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second2);

        mLicensePlate = findViewById(R.id.Dectect_text);
        mFirstPage = findViewById(R.id.FirstPage);
        mSecondPage = findViewById(R.id.SecondPage);
        mCamera = findViewById(R.id.cameraView);
        mGallery = findViewById(R.id.galleryView);
        mTextBox = findViewById(R.id.textBox);
        mYesBox = findViewById(R.id.yesView);
        mNoBox = findViewById(R.id.noView);
        mImageView = findViewById(R.id.image_view_chn);

        mYes = findViewById(R.id.Yes_btn);
        mNo = findViewById(R.id.No_btn);
        mChooseBtn = findViewById(R.id.gallery_Btn);
        mCameraBtn = findViewById(R.id.Camera_Btn);
        mDetectedtxtview = findViewById(R.id.Dectect_text);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        mChooseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //if system os is >= marshmallow, request runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        //permission not enabled, request it
                        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        //permission already granted
                        pickImageFromGallery();


                    }
                } else {
                    pickImageFromGallery();

                }

            }
        });

        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //if system os is >= marshmallow, request runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        //permission not enabled, request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        //permission already granted
                        openCamera();
                    }
                } else {
                    //system os < marshmallow
                    openCamera();

                }
            }

        });



        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetectedtxtview.setFocusableInTouchMode(true);
                mDetectedtxtview.setClickable(true);
                mDetectedtxtview.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        });

        //Yes button will check if license plate is found in database, if it is not, it goes to activity_staff_car, otherwise activity_other_vehicle

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLicensePlate();

            }
        });
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    pickImageFromGallery();
                    openCamera();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        ImagePath = image.getAbsolutePath();
        return image;
    }

    private void openCamera() {
        Log.d("Tag", "Open Camera start");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.d("Tag", "file created");
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.rplicenseplaterecognition.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE);
        }
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            mImageView.setImageURI(data.getData());

            runTextReconition();
            showNewPage();

        }
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK && data != null) {
            File f = new File(ImagePath);
            Bundle bundle = data.getExtras();
            mImageView.setImageURI(Uri.fromFile(f));
            Log.d("Tag", "Path of image is" + Uri.fromFile(f));

            //save to gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            runTextReconition();
            showNewPage();

        }
    }

    private void runTextReconition() {
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processTextReconitionResult(texts);
                Log.d("Tag", "text is" + texts);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                e.printStackTrace();
            }
        });

    }

    private void processTextReconitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("no text found");
            return;
        }

        String total_elements = "";
        String total_line = "";
        for (int i = 0; i < blocks.size(); i++) {
            String text_block = blocks.get(i).getText();


            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                String text_Lines = lines.get(j).getText();
                total_line = total_line + text_Lines;

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    total_elements = total_elements + elements.get(k).getText();
                    Log.d("Tag", "element level text " + (k) + ": " + total_elements);

                }
            }

        }
        mDetectedtxtview.setText(total_elements);

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showNewPage() {
        mSecondPage.setVisibility(View.VISIBLE);
        mFirstPage.setVisibility(View.GONE);
    }

    private void checkLicensePlate() {
        String text = mDetectedtxtview.getText().toString();
        mDatabase.child("StaffCar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(text)) {
                        Intent intent = new Intent (SecondActivity2.this, StaffCarPage.class);
                        intent.putExtra("LicensePlate", text);
                        startActivity(intent);

                        Toast.makeText(SecondActivity2.this, "Staff Vehicle detected", Toast.LENGTH_LONG).show();
                        Log.d("Tag", "found DB " + text);
                        Log.d("Tag", "CarMake  : " + snapshot.child(text).child("CarMake").getValue());
                        Log.d("Tag", "CarModel : " + snapshot.child(text).child("CarModel").getValue());
                        Log.d("Tag", "CarPark  : " + snapshot.child(text).child("CarparkNo").getValue());
                        Log.d("Tag", "Email    : " + snapshot.child(text).child("Email").getValue());
                        Log.d("Tag", "Name     : " + snapshot.child(text).child("Name").getValue());
                        Log.d("Tag", "Remarks  : " + snapshot.child(text).child("Remarks").getValue());
                        Log.d("Tag", "StaffID  : " + snapshot.child(text).child("StaffID").getValue());
                    } else {

                        Toast.makeText(SecondActivity2.this, "License Plate not found in database; Not a staff vehicle", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SecondActivity2.this, NotInDatabase.class);
                        intent.putExtra("LicensePlate", text);
                        startActivity(intent);
                    }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }



        });

    }

}