package com.example.rplicenseplaterecognition;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NotInDatabase extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_in_database);

        TextView mTextView = findViewById(R.id.LicensePlateShown);
        Button mButton = findViewById(R.id.Back);
        String licensePlate = (String) getIntent().getStringExtra("LicensePlate");
        mTextView.setText(licensePlate);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotInDatabase.this, SecondActivity2.class);
                startActivity(intent);
            }
        });
    }
}