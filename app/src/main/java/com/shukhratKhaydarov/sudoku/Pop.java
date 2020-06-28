package com.shukhratKhaydarov.sudoku;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class Pop extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popwindow);

        DisplayMetrics displayMatrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMatrics);

        int width= displayMatrics.widthPixels;
        int h=displayMatrics.heightPixels;

        getWindow().setLayout((int)(width),(int)(h*.8));

    }
}
