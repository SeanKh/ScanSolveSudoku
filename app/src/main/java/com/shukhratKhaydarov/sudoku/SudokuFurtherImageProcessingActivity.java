package com.shukhratKhaydarov.sudoku;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class SudokuFurtherImageProcessingActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_further_image_processing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Mat bw = SudokuImageProcessingActivity.resultWithEmptyCells1BinInvTrue.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues1binInvTrue.get(0)).clone();
        Bitmap img_bitmap = Bitmap.createBitmap(bw.cols(), bw.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw, img_bitmap);
        ImageView imageView = findViewById(R.id.img1);
        imageView.setImageBitmap(img_bitmap);

        Mat bw2 = SudokuImageProcessingActivity.resultWithEmptyCells1binInvFalse.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues1binInvFalse.get(0)).clone();
        Bitmap img_bitmap2 = Bitmap.createBitmap(bw2.cols(), bw2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw2, img_bitmap2);
        ImageView imageView2 = findViewById(R.id.img2);
        imageView2.setImageBitmap(img_bitmap2);


        Mat bw3 = SudokuImageProcessingActivity.resultWithEmptyCells2BinInvTrue.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues2binInvTrue.get(0)).clone();
        Bitmap img_bitmap3 = Bitmap.createBitmap(bw3.cols(), bw3.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw3, img_bitmap3);
        ImageView imageView3 = findViewById(R.id.img3);
        imageView3.setImageBitmap(img_bitmap3);

        Mat bw4 = SudokuImageProcessingActivity.resultWithEmptyCells2binInvFalse.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues2binInvFalse.get(0)).clone();
        Bitmap img_bitmap4 = Bitmap.createBitmap(bw4.cols(), bw4.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw4, img_bitmap4);
        ImageView imageView4 = findViewById(R.id.img4);
        imageView4.setImageBitmap(img_bitmap4);

        OpenCVLoader.initDebug();
    }

    public void forwardFirst(View v){
        Intent intent = new Intent(this, SudokuFurtherEditingImageProcessingActivity.class);
        intent.putExtra("chosen", 1);
        startActivity(intent);
    }

    public void forwardSecond(View view){
        Intent intent = new Intent(this, SudokuFurtherEditingImageProcessingActivity.class);
        intent.putExtra("chosen", 2);
        startActivity(intent);
    }

    public void forwardThird(View v){
        Intent intent = new Intent(this, SudokuFurtherEditingImageProcessingActivity.class);
        intent.putExtra("chosen", 3);
        startActivity(intent);
    }

    public void forwardFourth(View view){
        Intent intent = new Intent(this, SudokuFurtherEditingImageProcessingActivity.class);
        intent.putExtra("chosen", 4);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        for (int i = 0; i < menu.size(); i++)
            menu.getItem(i).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            Intent intent = new Intent(SudokuFurtherImageProcessingActivity.this, MainActivity.class);

            startActivity(intent);
            return true;
        }

        if (id == R.id.hint) {
            Intent intent = new Intent(SudokuFurtherImageProcessingActivity.this, Pop.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
