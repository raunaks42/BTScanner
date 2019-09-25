package com.blazingtrail.btscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class filter extends AppCompatActivity {

    Button next;

    ImageView currIv;

    Bitmap original;
    Bitmap grayscale;
    Bitmap bw;
    Bitmap lighten;

    String name;

    Bitmap curr=original;

    Uri image_uri;

    int ch;
    int count;

    SQLiteDatabase db;

    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        db = openOrCreateDatabase("UNIvents",Context.MODE_PRIVATE, null);
        next=findViewById(R.id.next);
        Intent i=getIntent();
        Uri uri = i.getParcelableExtra("imageUri");
        name=i.getStringExtra("name");
        ch=i.getIntExtra("choice",0);
        count=i.getIntExtra("count",0);
        if(ch==0) {
            next.setEnabled(false);
        }
        currIv=findViewById(R.id.currImg);
        process(uri);
    }

    public void next(View view) {
        db.execSQL("INSERT INTO DocImages VALUES (" +
                count + ", " + name.split("/")[name.split("/").length-1] + ", NULL" +
                ");");
        File file = new File(name+"/"+count+".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            curr.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Image Source");
        dialog.setCancelable(false);
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0) {
                    pickCamera();
                }
                if (which==1) {
                    pickGallery();
                }
            }
        });
        dialog.create().show();
    }

    public void done(View view) {
        String ext="";
        if(ch==1) {
            db.execSQL("INSERT INTO DocImages VALUES("+count+","+name.split("/")[name.split("/").length-1]+",NULL);");
            ext="/"+count+".jpg";
        }
        File file = new File(name+ext);
        try {
            FileOutputStream out = new FileOutputStream(file);
            curr.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent i = new Intent(this , tags.class);
        i.putExtra("name",name);
        i.putExtra("choice",ch);
        startActivity(i);
    }

    public void original(View view) {
        currIv.setImageBitmap(original);
        curr=original;
    }

    public void grayscale(View view) {
        currIv.setImageBitmap(grayscale);
        curr=grayscale;
    }

    public void bw(View view) {
        currIv.setImageBitmap(bw);
        curr=bw;
    }

    public void lighten(View view) {
        currIv.setImageBitmap(lighten);
        curr=lighten;
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
                Intent i = new Intent(this , filter.class);
                i.putExtra("imageUri",resultUri);
                i.putExtra("name",name);
                i.putExtra("choice",ch);
                i.putExtra("count",count+1);
                startActivity(i);
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void process(Uri uri) {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText("Loading ...");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        dialog.show();
        try {
            original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            Mat morg=new Mat();
            Mat m=new Mat();
            Utils.bitmapToMat(original,morg);
            Imgproc.cvtColor(morg,m,COLOR_BGR2GRAY);
            Utils.matToBitmap(m, grayscale);

            Imgproc.GaussianBlur(m,m,new Size(11,11),0);
            Imgproc.threshold(m,m,0,255,THRESH_BINARY|THRESH_OTSU);
            Mat k1 = getStructuringElement(MORPH_RECT, new Size(15, 15));
            Imgproc.dilate(m, m, k1);
            Imgproc.erode(m, m, k1);
            Utils.matToBitmap(m, bw);

            Mat newImage = Mat.zeros(morg.size(), morg.type());
            double alpha = 2.0; /*< Simple contrast control */
            int beta = 30;       /*< Simple brightness control */
            byte[] imageData = new byte[(int) (morg.total()*morg.channels())];
            morg.get(0, 0, imageData);
            byte[] newImageData = new byte[(int) (newImage.total()*newImage.channels())];
            for (int y = 0; y < morg.rows(); y++) {
                for (int x = 0; x < morg.cols(); x++) {
                    for (int c = 0; c < morg.channels(); c++) {
                        double pixelValue = imageData[(y * morg.cols() + x) * morg.channels() + c];
                        pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
                        newImageData[(y * morg.cols() + x) * morg.channels() + c]
                                = saturate(alpha * pixelValue + beta);
                    }
                }
            }
            newImage.put(0, 0, newImageData);
            Utils.matToBitmap(newImage, lighten);

        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView originalIv=findViewById(R.id.original);
        originalIv.setImageBitmap(original);
        ImageView grayscaleIv=findViewById(R.id.grayscale);
        grayscaleIv.setImageBitmap(grayscale);
        ImageView bwIv=findViewById(R.id.bw);
        bwIv.setImageBitmap(bw);
        ImageView lightenIv=findViewById(R.id.lighten);
        lightenIv.setImageBitmap(lighten);
        currIv.setImageBitmap(original);
        dialog.dismiss();
    }

    private byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }
}
