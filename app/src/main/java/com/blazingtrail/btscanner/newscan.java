package com.blazingtrail.btscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class newscan extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    String name;

    int choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newscan);

        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    public void newimage(View view) {
        choice=0;
        getName(choice);
        showImageImportDialog();
    }

    public void newdoc(View view) {
        choice=1;
        getName(choice);
        showImageImportDialog();
    }

    public void newocr(View view) {
        choice=2;
        getName(choice);
        showImageImportDialog();
    }

    private void getName(int choice) {
        String title;
        switch(choice) {
            case 0: title="Image Name";
                    break;
            case 1: title="Document Name";
                    break;
            case 2: title="OCR Image Name";
                    break;
            default:title="Image Name";
                    break;
        }
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setView(input);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                name = input.getText().toString();
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

    private void showImageImportDialog() {
        SQLiteDatabase db;
        db = openOrCreateDatabase("UNIvents", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Images (" +
                "iname VARCHAR(32) PRIMARY KEY, " +
                "text VARCHAR(1024), " +
                "timestamp DATETIME NOT NULL" +
                ");" );
        db.execSQL("CREATE TABLE IF NOT EXISTS Documents (" +
                "dname VARCHAR(32) PRIMARY KEY, " +
                "timestamp DATETIME NOT NULL" +
                ");" );
        db.execSQL("CREATE TABLE IF NOT EXISTS DocImages (" +
                "diname VARCHAR(4), " +
                "dname VARCHAR(32), " +
                "text VARCHAR(1024)," +
                "PRIMARY KEY (diname, dname)" +
                ");");
        db.execSQL("CREATE TABLE IF NOT EXISTS Tags (" +
                "name VARCHAR(32), " +
                "tag VARCHAR(32), " +
                "PRIMARY KEY (name, cat)" +
                ");");
        db.execSQL("CREATE TABLE IF NOT EXISTS Categories (" +
                "name VARCHAR(32), " +
                "cat VARCHAR(32), " +
                "PRIMARY KEY (name, cat)" +
                ");");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        String dt=sdf.format(Calendar.getInstance().getTime());
        String root = Environment.getExternalStorageDirectory().toString();
        String dir;
        String ext;
        if(choice==0||choice==2) {
            dir="Images";
            ext=".jpg";
            db.execSQL("INSERT INTO Images VALUES (" +
                    name+", NULL, "+dt+
                    ");");
        }
        else {
            dir="Documents";
            ext="";
            db.execSQL("INSERT INTO Documents VALUES (" +
                    name+", "+dt+
                    ");");
        }
        File myDir = new File(root + "/BTscanner/"+dir);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String newname=name+ext;
        File newf = new File (myDir, newname);
        if(newf.exists()) {
            newf.delete();
        }
        if(choice==1) {
            newf.mkdirs();
        }
        name=root+"/BTscanner/"+dir+"/"+newname;

        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Image Source");
        dialog.setCancelable(false);
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickCamera();
                    }
                }
                if (which==1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return result&&result1;
    }

    private boolean checkStoragePermission() {
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return result1;
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0) {
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted) {
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0) {
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK) {
            if (requestCode==IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK) {
                Uri resultUri=result.getUri();
                if (choice==0||choice==1){
                    Intent i = new Intent(this , filter.class);
                    i.putExtra("imageUri",resultUri);
                    i.putExtra("name",name);
                    i.putExtra("choice",choice);
                    i.putExtra("count",0);
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(this, ocr.class);
                    i.putExtra("imageUri",resultUri);
                    i.putExtra("name",name);
                    startActivity(i);
                }

                /*
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
