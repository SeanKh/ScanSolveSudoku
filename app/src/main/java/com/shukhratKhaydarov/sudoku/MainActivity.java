package com.shukhratKhaydarov.sudoku;

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

import com.shukhratKhaydarov.sudoku.SudokuSolver.SudokuSolverMainActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;
    static final Integer CAMERA_EXST  = 0x5;
    public static final int PICK_IMAGE = 1;
    public static final int CAMERA_REQUEST = 1888;
    private Boolean exit = false;
    private static final int gallery=12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        OpenCVLoader.initDebug();
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
    }

    /**
     * Permissions are requested
     * @param   permission    text for permission
     * @param   requestCode    request code for the permission
     */
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
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            if(permission.equals(Manifest.permission.CAMERA)){
                Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
                photo.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(photo,CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(permissions.length!=0) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
                if(requestCode==5){
                    Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
                    photo.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(photo,CAMERA_REQUEST);
                }
            } else {
                this.finishAffinity();
            }
        }
    }


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
        }
        if (requestCode == gallery && resultCode == RESULT_OK && data != null) {
            Uri uploadfileuri = data.getData();
            Intent intent = new Intent(MainActivity.this, SudokuSolverMainActivity.class);
            intent.putExtra("image-path", uploadfileuri.toString());
            startActivity(intent);
        }
    }


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

    /**
     * Loads picture from device storage
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void loadPicture(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    /**
     * Takes picture using device camera
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void takePicture(View view){
        askForPermission(Manifest.permission.CAMERA,CAMERA_EXST);
    }

    /**
     * Loads file from device storage
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void loadFile(View view){
        String[] mimeTypes = {"text/plain"};
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

    /**
     * Creates and starts intent to open manually entering activity
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void enterManually(View view){
        Intent intent = new Intent(this, SudokuSolverMainActivity.class);
        intent.putExtra("fromMainActivity", true);
        startActivity(intent);
    }
}
