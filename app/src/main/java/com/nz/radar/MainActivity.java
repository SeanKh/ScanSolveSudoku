package com.nz.radar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
    }

    /** Called when the user taps the Send button */
    public void openIP(View view) {
        Intent intent = new Intent(this, SudokuImageProcessingActivity.class);

        startActivity(intent);
    }

    public static final int PICK_IMAGE = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {

            Uri imageUri = data.getData();/* This is the Uri you got from the gallery */
            Intent intent = new Intent(MainActivity.this, SudokuImageProcessingActivity.class);
            intent.putExtra("image-uri", imageUri.toString());
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

    }

    public void loadFile(View v){

    }

    public void loadSavedSolution(View v){

    }

    public void enterManually(View v){

    }


}
