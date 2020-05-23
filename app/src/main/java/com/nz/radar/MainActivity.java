package com.nz.radar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nz.radar.SudokuSolver.SudokuSolverMainActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        OpenCVLoader.initDebug();
        checkPermission();
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Access Permission Info");
                alertBuilder.setMessage("App needs permission to access images, files on your phone.");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{permission},
                                requestCode);
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                //ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {

            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;
    static final Integer CAMERA_EXST  = 0x5;
    public void checkPermission(){

        /*AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage("App needs permission to read files.");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);

            }
        });

        AlertDialog alert = alertBuilder.create();
        alert.show();*/

        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

        //askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if(permissions.length!=0) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    //Location

                    //Write external Storage
                    case 3:
                        break;
                    //Read External Storage
                    case 4:
                        /*Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(imageIntent, 11);*/
                        break;
                    //Camera
                    case 5:
                        Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
                        photo.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(photo,CAMERA_REQUEST);
                        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, 12);
                        }*/
                        break;

                }

                //Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "App will launch only with granted permission", Toast.LENGTH_SHORT).show();
                //checkPermission();
                this.finishAffinity();
            }
        }
    }
    public static final int PICK_IMAGE = 1;
    public static final int CAMERA_REQUEST = 1888;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();/* This is the Uri you got from the gallery */
            Intent intent = new Intent(MainActivity.this, SudokuImageProcessingActivity.class);
            intent.putExtra("image-uri", imageUri.toString());
            startActivity(intent);

        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "photo.jpg");
            Uri uri = Uri.fromFile(file);

            Intent intent = new Intent(MainActivity.this, SudokuImageProcessingActivity.class);
            intent.putExtra("image-uri", uri.toString());
            startActivity(intent);

            /*Bitmap photo = (Bitmap) data.getExtras().get("data");

            Intent intent = new Intent(this, SudokuImageProcessingActivity.class);
            intent.putExtra("photo", photo);
            startActivity(intent);*/
        }
        if (requestCode == gallery && resultCode == RESULT_OK && data != null) {
            Uri uploadfileuri = data.getData();

            Intent intent = new Intent(MainActivity.this, SudokuSolverMainActivity.class);
            intent.putExtra("image-path", uploadfileuri.toString());
            startActivity(intent);

        }
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            //finish(); // finish activity
            this.finishAffinity();
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    public void loadPicture(View v){

        /*Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void takePicture(View v){
        askForPermission(Manifest.permission.CAMERA,CAMERA_EXST);

    }
    private static final int gallery=12;

    public void loadFile(View v){
        String[] mimeTypes =
                {"text/plain"
                        };

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), gallery);

    }

    public void enterManually(View v){
        Intent intent = new Intent(this, SudokuSolverMainActivity.class);

        startActivity(intent);
    }


}
