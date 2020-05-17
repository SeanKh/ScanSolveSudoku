package com.nz.radar.SudokuSolver;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.nz.radar.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SudokuSolverMainActivity extends AppCompatActivity implements View.OnClickListener {
    GridView gridview;
    AssetManager assetManager;
    int puzzleLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_solver);

        assetManager = getAssets();
        gridview = (GridView) findViewById(R.id.gridView);

        Intent intent = getIntent();
        String path = intent.getStringExtra("image-path");

        try {
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(file);

            //InputStream inputStream = assetManager.open(path);
            gridview.setAdapter(new TextAdapter(this, inputStream));
        } catch (IOException e) {
            Log.i("Yo", "OH NO");
        }

        Button mClickButton1 = (Button)findViewById(R.id.one);
        mClickButton1.setOnClickListener(this);
        Button mClickButton2 = (Button)findViewById(R.id.two);
        mClickButton2.setOnClickListener(this);
        Button mClickButton3 = (Button)findViewById(R.id.three);
        mClickButton3.setOnClickListener(this);
        Button mClickButton4 = (Button)findViewById(R.id.four);
        mClickButton4.setOnClickListener(this);
        Button mClickButton5 = (Button)findViewById(R.id.five);
        mClickButton5.setOnClickListener(this);
        Button mClickButton6 = (Button)findViewById(R.id.six);
        mClickButton6.setOnClickListener(this);
        Button mClickButton7 = (Button)findViewById(R.id.seven);
        mClickButton7.setOnClickListener(this);
        Button mClickButton8 = (Button)findViewById(R.id.eight);
        mClickButton8.setOnClickListener(this);
        Button mClickButton9 = (Button)findViewById(R.id.nine);
        mClickButton9.setOnClickListener(this);
        Button mClickButton0 = (Button)findViewById(R.id.zero);
        mClickButton0.setOnClickListener(this);
    }

    public void populateGrid(View view) {
        try {
            InputStream inputStream = assetManager.open("empty.in");
            gridview.setAdapter(new TextAdapter(this, inputStream));
            puzzleLevel = 0;
        } catch (IOException e) {
            Log.i("Yo", "OH NO");

        }

    }

    public static Integer[][] make2d(Integer[] var){
        Integer[][] out = new Integer[9][9];
        for(int n=0;n<var.length;n++){
            out[n/9][n%9]=var[n];
        }
        return out;
    }

    public void solvePuzzle(View view) {

        if(validInput(view)) {
            TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();
            SudokuSolver sudokuSolver = new SudokuSolver();
            Integer[][] temp = make2d(textAdapter.grid);

            textAdapter.grid2d = temp;

            boolean solved = sudokuSolver.solve(0, 0, textAdapter.grid2d);
            int x = 0;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    TextView textView = (TextView) gridview.getChildAt(x);
                    textView.setText(String.valueOf(textAdapter.grid2d[i][j]));
                    x++;
                }
            }
        }

    }

    public void emptyCell(View view){

    }


    public void indicateWrongCells(SudokuSolver sudokuSolver, TextAdapter textAdapter,View view, int loopIndex, int loopLimit,int loopIncrement){
        List<String> wrongNumbers=new ArrayList<>();

        EditText textView;
        for(int n=0;n<81;n++)
        {
            textView = (EditText) gridview.getChildAt(n);
            textView.setTextColor(Color.BLACK);
        }
        for(int n=loopIndex;n<loopLimit;n+=loopIncrement){
            for(int j=loopIndex;j<loopLimit;j+=loopIncrement){
                if(n!=j) {
                    if(textAdapter.grid[n]!=0 || textAdapter.grid[j]!=0) {
                        textView = (EditText) gridview.getChildAt(n);

                        if (textAdapter.grid[j] == textAdapter.grid[n]) {

                            textView.setTextColor(Color.RED);
                        }
                    }
                }
            }
        }

        String toastMsg="";
        for(String e :wrongNumbers){
            toastMsg+=e+", ";
        }
        Toast.makeText(view.getContext(), "Number "+toastMsg+" is wrong",
                Toast.LENGTH_LONG).show();
    }



    public boolean validInput(View view) {

        TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();

        SudokuSolver sudokuSolver = new SudokuSolver();

        sudokuSolver.valid_board(textAdapter.grid2d);

        boolean valid=sudokuSolver.getValid();


        if(valid==false){
            if(sudokuSolver.getRowOrCol()!=null) {
                if (sudokuSolver.getRowOrCol().equals("row")) {
                    indicateWrongCells(sudokuSolver, textAdapter, view, 9 * sudokuSolver.getInvalidLine(), 9 * sudokuSolver.getInvalidLine() + 9, 1);
                } else if (sudokuSolver.getRowOrCol().equals("col")) {
                    indicateWrongCells(sudokuSolver, textAdapter, view, sudokuSolver.getInvalidLine(), 72 + sudokuSolver.getInvalidLine() + 1, 9);
                }
            }
            else {
                EditText textView;
                for(int n=0;n<81;n++)
                {
                    textView = (EditText) gridview.getChildAt(n);
                    textView.setTextColor(Color.BLACK);
                }
                textView = (EditText) gridview.getChildAt(sudokuSolver.getInvalidRowInBox()*9+sudokuSolver.getInvalidColInBox());
                textView.setTextColor(Color.RED);
                Toast.makeText(view.getContext(), "Number "+textView.getText()+" in box is wrong",
                        Toast.LENGTH_LONG).show();
            }
            return false;
        }
        else{
            for(int n=0;n<81;n++)
            {
                EditText textView = (EditText) gridview.getChildAt(n);
                textView.setTextColor(Color.BLACK);

            }
            return true;
        }

    }


    @Override
    public void onClick(View v) {
        TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();

        switch (v.getId()){
            case R.id.one:
                textAdapter.setClickedButton(1);
                break;
            case R.id.two:
                textAdapter.setClickedButton(2);
                break;
            case R.id.three:
                textAdapter.setClickedButton(3);
                break;
            case R.id.four:
                textAdapter.setClickedButton(4);
                break;
            case R.id.five:
                textAdapter.setClickedButton(5);
                break;
            case R.id.six:
                textAdapter.setClickedButton(6);
                break;
            case R.id.seven:
                textAdapter.setClickedButton(7);
                break;
            case R.id.eight:
                textAdapter.setClickedButton(8);
                break;
            case R.id.nine:
                textAdapter.setClickedButton(9);
                break;
            case R.id.zero:
                textAdapter.setClickedButton(0);
                break;
            default:
                textAdapter.setClickedButton(10);
                break;
        }
        textAdapter.updateGrid();

    }
}

