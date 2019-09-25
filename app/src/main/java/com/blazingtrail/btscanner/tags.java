package com.blazingtrail.btscanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class tags extends AppCompatActivity {

    String name;
    String fname;
    String newentry;
    int choice;
    SQLiteDatabase db;

    ArrayList<String> taglist;
    ArrayList<String> catlist;

    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;

    ListView taglw;
    ListView catlw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        Intent i=getIntent();
        name=i.getStringExtra("name");
        choice=i.getIntExtra("choice",0);

        db = openOrCreateDatabase("UNIvents", Context.MODE_PRIVATE, null);
        taglist=new ArrayList<>();
        catlist=new ArrayList<>();
        showtags();
        showcats();

        catlw=findViewById(R.id.catlist);
        taglw=findViewById(R.id.taglist);

        fname=name.split("/")[name.split("/").length-1];
        if(choice==0) {
            fname = fname.split(".")[0];
        }
    }

    public void addtag(View view) {
        addpopup();
        db.execSQL("INSERT INTO Tags VALUES (" +
                fname + ", " + newentry +
                ");");
        taglist.clear();
        showtags();
    }

    public void addcat(View view) {
        addpopup();
        db.execSQL("INSERT INTO Categories VALUES (" +
                fname + ", " + newentry +
                ");");
        catlist.clear();
        showcats();
    }

    public void showtags() {
        Cursor cursor= db.rawQuery("SELECT tag from Tags WHERE name=" + fname + ";" ,null);
        if (cursor.getCount()!=0) {
            while(cursor.moveToNext()) {
                taglist.add(cursor.getString(0));
            }
            adapter1=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,taglist){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view =super.getView(position, convertView, parent);
                    TextView textView=(TextView) view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.WHITE);
                    return view;
                }
            };
            taglw.setAdapter(adapter1);
        }
    }

    public void showcats() {
        Cursor cursor= db.rawQuery("SELECT cat from Categories WHERE name=" + fname + ";" ,null);
        if (cursor.getCount()!=0) {
            while(cursor.moveToNext()) {
                catlist.add(cursor.getString(0));
            }
            adapter2=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,catlist){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view =super.getView(position, convertView, parent);
                    TextView textView=(TextView) view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.WHITE);
                    return view;
                }
            };
            catlw.setAdapter(adapter2);
        }
    }

    public void addpopup() {
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("New");
        dialog.setView(input);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newentry = input.getText().toString();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    public void finish(View view) {
        Intent i = new Intent(this , home.class);
        startActivity(i);
    }
}
