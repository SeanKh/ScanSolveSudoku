package com.nz.radar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

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
    }

    /** Called when the user taps the Send button */
    public void openIP(View view) {
        Intent intent = new Intent(this, SudokuImageProcessingActivity.class);

        startActivity(intent);
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
        Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
        photo.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(photo,CAMERA_REQUEST);
    }
    private static final int gallery=12;

    public void loadFile(View v){
        String type="*/*";

        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(type);
        startActivityForResult(Intent.createChooser(i,"select file") ,gallery);
    }

    public void enterManually(View v){
        Intent intent = new Intent(this, SudokuSolverMainActivity.class);

        startActivity(intent);
    }


}
