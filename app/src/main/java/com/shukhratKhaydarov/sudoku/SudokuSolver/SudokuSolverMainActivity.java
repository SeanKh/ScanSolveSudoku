package com.shukhratKhaydarov.sudoku.SudokuSolver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.base.Joiner;
import com.shukhratKhaydarov.sudoku.Board;
import com.shukhratKhaydarov.sudoku.MainActivity;
import com.shukhratKhaydarov.sudoku.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SudokuSolverMainActivity extends AppCompatActivity implements View.OnClickListener {
    GridView gridview;
    AssetManager assetManager;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_solver);

        assetManager = getAssets();
        gridview = (GridView) findViewById(R.id.gridView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(intent.hasExtra("image-path")) {
            String path = intent.getStringExtra("image-path");
            if(path.substring(0,8).equals("file:///")) {
                path = path.substring(8);
            }
            try {
                File file = new File(path);
                FileInputStream inputStream = new FileInputStream(file);
                gridview.setAdapter(new TextAdapter(this, inputStream));
            } catch (IOException e) {
                Log.i("Yo", "OH NO");
            }
        }
        else if(intent.hasExtra("fromMainActivity") ){

            intent.removeExtra("fromMainActivity");
            try {
                InputStream inputStream = assetManager.open("empty.in");
                gridview.setAdapter(new TextAdapter(this, inputStream));
            } catch (IOException e) {
                Log.i("Yo", "OH NO");
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent intent = new Intent(SudokuSolverMainActivity.this, MainActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Empties all sudoku grid cells
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void emptyGrid(View view) {
        try {
            TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();
            if(textAdapter.getTextViewNeeded()!=null) textAdapter.getTextViewNeeded().clearAnimation();
            InputStream inputStream = assetManager.open("empty.in");
            gridview.setAdapter(new TextAdapter(this, inputStream));

        } catch (IOException e) {
            Log.i("Yo", "OH NO");
        }
    }

    /**
     * Saves the text into file with passed file name
     * @param  text  text to be saved in file
     * @param  FILE_NAME file name of the file, which will contain the supplied text
     * @return      the full path to the saved file
     */
    public String save(String text,String FILE_NAME) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try
        {
            File dir = new File(fullPath);

            if (!dir.getParentFile().exists()) {
                dir.getParentFile().mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, FILE_NAME);
            if(file.getParentFile().exists())
                file.getParentFile().delete();
            file.getParentFile().createNewFile();
            fOut = new FileOutputStream(file);
            fOut.write(text.getBytes());
            fOut.flush();
            fOut.close();
            fullPath+="/"+FILE_NAME;
            Toast.makeText(this, "Saved to " + fullPath,
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
        return fullPath;
    }

    /**
     * Constructs 2 dimensional Integer array
     * @param  var  1 dimensional Integer array
     * @return      2 dimensional Integer array
     */
    public static Integer[][] make2d(Integer[] var){
        Integer[][] out = new Integer[9][9];
        for(int n=0;n<var.length;n++){
            out[n/9][n%9]=var[n];
        }
        return out;
    }

    /**
     * Solves all Sudoku cells
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void solvePuzzle(View view) {
        if(validInput(view)) {
            TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();
            SudokuSolver sudokuSolver = new SudokuSolver();
            Integer[][] temp = make2d(textAdapter.grid);

            textAdapter.grid2d = temp;

            sudokuSolver.solve(0, 0, textAdapter.grid2d);
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

    /**
     * Solves random cell of Sudoku board
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void solveNextCell(View view){
        Random random=new Random();
        int randomNumber=random.nextInt(80);
        if(validInput(view)) {
            TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();
            SudokuSolver sudokuSolver = new SudokuSolver();
            Integer[][] temp = make2d(textAdapter.grid);

            textAdapter.grid2d = temp;

            sudokuSolver.solve(0, 0, textAdapter.grid2d);

            loop(randomNumber, textAdapter);
        }
    }

    /**
     * Assigns value to the random cell of Sudoku board
     * @param  randomNumber  the cell that will contain the random number
     * @param  textAdapter  the value that will be assigned to random cell
     */
    public void loop(int randomNumber,TextAdapter textAdapter){
        int x = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if(x==randomNumber) {
                    TextView textView = (TextView) gridview.getChildAt(x);
                    textView.setText(String.valueOf(textAdapter.grid2d[i][j]));

                    return;
                }
                x++;
            }
        }
    }

    /**
     * Saves the solution at device storage
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     */
    public void saveSolution(View view){
        TextAdapter textAdapter = (TextAdapter) gridview.getAdapter();
        List<Integer> list=new ArrayList<>();
        for(int n=0;n<9;n++){
            for(int z=0;z<9;z++){
                list.add(textAdapter.grid2d[n][z]);
            }
        }
        Board b = Board.of(9, Joiner.on(" ").join(list));

        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        String FILENAME = "resultAfterSolving"+sdf.format(timestamp)+".txt";
        save(b.toString(),FILENAME);
    }

    /**
     * Indicates the wrong cells
     * @param  textAdapter  instance of TextAdapter Class, where sudoku board cells are stored
     * @param  view  view object of Class View is required for showing Toast message
     * @param  loopIndex  the index for required sudoku cell
     * @param  loopLimit  the limit for the required sudoku cells
     * @param  loopIncrement  the loop increment for the required sudoku cells
     */
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

    /**
     * Checks the inputted sudoku cells
     * @param  view  view object of Class View is required, because method needs to be visible from corresponding XML file
     * @return      boolean value indicating the validity of inputted sudoku cells
     */
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
        if(textAdapter.getClickedOnCell()==true){
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
        }
        textAdapter.updateGrid();
    }
}

