package com.example.rplicenseplaterecognition;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class StaffCarPage extends AppCompatActivity {

    TextView mLicensePlate;

    EditText mCarMakeModel;
    EditText mStaffName;
    EditText mStaffEmail;
    EditText mStaffId;
    EditText mRemarks;

    Spinner mCarparkNo;

    Button mSaveDatabase;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_car_page);

        String licensePlate = (String) getIntent().getStringExtra("LicensePlate");
        mLicensePlate = findViewById(R.id.LicensePlate);

        mCarMakeModel = findViewById(R.id.CarMakeModel);
        mStaffName = findViewById(R.id.StaffName);
        mStaffEmail = findViewById(R.id.StaffEmail);
        mStaffId = findViewById(R.id.StaffID);
        mRemarks = findViewById(R.id.Remarks);

        mCarparkNo = findViewById(R.id.CarparkNoSpinner);

        mSaveDatabase = findViewById(R.id.SaveDatabase);

        mLicensePlate.setText(licensePlate);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getData();
        Log.d("Tag", "Called database");

        mSaveDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDatabase();

            }
        });
    }

    private void getData() {
        String plateNumber = mLicensePlate.getText().toString();
        mDatabase.child("StaffCar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                DataSnapshot table = snapshot.child(plateNumber);

                String carMakeModel = table.child("CarMake").getValue().toString() + ", " + table.child("CarModel").getValue().toString();
                mCarMakeModel.setText(carMakeModel);

                String staffName = table.child("Name").getValue().toString();
                mStaffName.setText(staffName);

                String staffId = table.child("StaffID").getValue().toString();
                mStaffId.setText(staffId);

                String staffEmail = table.child("Email").getValue().toString();
                mStaffEmail.setText(staffEmail);
                Log.d("Tag", "Called" + table.child("CarMake").getValue());

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }
    private void saveDatabase() {
            String remarks = mRemarks.getText().toString();
            String plateNumber = mLicensePlate.getText().toString();
            String carpark = mCarparkNo.getSelectedItem().toString();

            mDatabase.child("StaffCar").child(plateNumber).child("Remarks").setValue(remarks);
            mDatabase.child("StaffCar").child(plateNumber).child("CarparkNo").setValue(carpark);
        Intent intent = new Intent(StaffCarPage.this, SecondActivity2.class);

        Toast.makeText(StaffCarPage.this,"Remarks and carpark number updated" , Toast.LENGTH_LONG).show();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(200); // As I am using LENGTH_LONG in Toast
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        Log.d("Tag", "Saved to database");
    }
}