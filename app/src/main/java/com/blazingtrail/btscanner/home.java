package com.blazingtrail.btscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void myimages(View view) {
        //Intent i = new Intent(this , UserSignup.class);
        //startActivity(i);
    }

    public void mydocs(View view) {
        //Intent i = new Intent(this , UserSignup.class);
        //startActivity(i);
    }

    public void newscan(View view) {
        Intent i = new Intent(this , newscan.class);
        startActivity(i);
    }
}
