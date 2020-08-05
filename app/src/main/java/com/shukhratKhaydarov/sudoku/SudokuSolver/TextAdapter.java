package com.shukhratKhaydarov.sudoku.SudokuSolver;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.shukhratKhaydarov.sudoku.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextAdapter extends BaseAdapter {
    private Context mContext;
    public Integer[] grid = new Integer[81];
    public Integer[][] grid2d = new Integer[9][9];
    public int gridCellClicked=100;
    public TextView textViewNeeded;
    public int clickedButton=10;
    boolean clickedOnCell=false;
    /**
     * Class constructor.
     */
    public TextAdapter(Context c, InputStream inputStream) throws IOException {
        mContext = c;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int i = 0;
        int row = 0, col = 0;
        while((line = in.readLine()) != null){
            String[] lineArray = line.split(" ");
            for (String element : lineArray) {
                //element=element.replaceAll("\\s+","");
                if(element.equals(" ") || element.equals("")){}
                else if (element.equals("0")) {
                    grid[i] = 0;
                    if (col % 9 == 0 && col != 0) {
                        col = 0;
                        row++;
                    }
                    grid2d[row][col] = 0;
                }
                else {
                    if (col % 9 == 0 && col != 0) {
                        col = 0;
                        row++;
                    }
                    if(element.equals("null")){element="0";}
                    grid[i] = Integer.valueOf(element);
                    grid2d[row][col] = Integer.valueOf(element);
                }
                col++;
                i++;
            }
        }
    }


    /**
     * Gets textViewNeeded
     * @return      value of textViewNeeded
     */
    public TextView getTextViewNeeded() {
        return textViewNeeded;
    }

    /**
     * Sets textViewNeeded
     * @param   textViewNeeded   new value for textViewNeeded
     */
    public void setTextViewNeeded(TextView textViewNeeded) {
        this.textViewNeeded = textViewNeeded;
    }

    /**
     * Gets clickedButton
     * @return      value of clickedButton
     */
    public int getClickedButton() {
        return clickedButton;
    }

    /**
     * Sets clickedButton
     * @param   clickedButton   new value for clickedButton
     */
    public void setClickedButton(int clickedButton) {
        this.clickedButton = clickedButton;
    }

    /**
     * Gets gridCellClicked
     * @return      value of gridCellClicked
     */
    public int getGridCellClicked() {
        return gridCellClicked;
    }

    /**
     * Sets gridCellClicked
     * @param   gridCellClicked   new value for gridCellClicked
     */
    public void setGridCellClicked(int gridCellClicked) {
        this.gridCellClicked = gridCellClicked;
    }

    /**
     * Gets clickedOnCell
     * @return      value of clickedOnCell
     */
    public boolean getClickedOnCell() {
        return clickedOnCell;
    }

    /**
     * Gets 81
     * @return      value of 81
     */
    public int getCount() {
        return 81;
    }

    /**
     * Gets null
     * @return      null
     */
    public Object getItem(int position) {
        return null;
    }

    /**
     * Gets position
     * @return      0
     */
    public long getItemId(int position) {
        return 0;
    }


    /**
     * Creates a new ImageView for each item referenced by the Adapter
     * @param   position   position of sudoku cell
     * @param   convertView   view, where sudoku board is located
     * @param   parent    parent viewgroup
     * @return      text of updated sudoku cell
     */
    public View getView(final int position, final View convertView, ViewGroup parent) {
        EditText textView;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new EditText(mContext);
            textView.setLayoutParams(new GridView.LayoutParams(85, 85));
            textView.setPadding(0, 0, 0, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            GradientDrawable gd = new GradientDrawable();
            gd.setStroke(1, 0xFF000000);
            textView.setBackgroundDrawable(gd);

            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(1);
            textView.setFilters(FilterArray);
        } else {
            textView = (EditText) convertView;
        }
        if (grid[position] != 0) {
            textView.setEnabled(true);
            textView.setText(String.valueOf(grid[position]));
        }
        textView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(getTextViewNeeded()!=null) getTextViewNeeded().clearAnimation();
                    if(getTextViewNeeded()!=textView) clickedOnCell=false;
                    setGridCellClicked(position);
                    Animation animation= AnimationUtils.loadAnimation(view.getContext(), R.anim.scale);
                    textView.clearAnimation();
                    if(clickedOnCell==true)
                    {
                        textView.clearAnimation();
                        clickedOnCell=false;
                    }
                    else{
                        textView.startAnimation(animation);
                        clickedOnCell=true;
                    }
                    setTextViewNeeded(textView);
                    updateGrid();
                }
                return true;
            }
        });
        return textView;
    }

    /**
     * Updates Sudoku grid cells
     */
    @SuppressWarnings("ResourceAsColor")
    public void updateGrid(){
        if(getGridCellClicked()!=100) {
            if (getClickedButton() != 10 && clickedOnCell == true) {
                grid[getGridCellClicked()] = getClickedButton();
                grid2d = SudokuSolverMainActivity.make2d(grid);
                if (getClickedButton() == 0) {
                    getTextViewNeeded().setText("");
                } else getTextViewNeeded().setText(String.valueOf(grid[getGridCellClicked()]));
                setClickedButton(10);
                //getTextViewNeeded().setBackgroundColor(Color.TRANSPARENT);
                getTextViewNeeded().clearAnimation();

            }
        }
    }
}

