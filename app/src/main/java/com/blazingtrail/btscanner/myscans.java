package com.blazingtrail.btscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class myscans extends AppCompatActivity {

    private ArrayList<String> names=new ArrayList<>();
    private ArrayList<String> tags=new ArrayList<>();
    private ArrayList<String> cats=new ArrayList<>();
    private ArrayList<String> dates=new ArrayList<>();
    private ArrayList<Bitmap> images=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myscans);

        File dir= new File(Environment.getExternalStorageDirectory().toString()+"/BTscanner/Images");

        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    names.add(file);

        }
        Collections.sort(names);




        RecyclerView rv=findViewById(R.id.rv);
        RecyclerViewAdapter adapter=new RecyclerViewAdapter(names,tags,cats,dates,images,this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }
}
