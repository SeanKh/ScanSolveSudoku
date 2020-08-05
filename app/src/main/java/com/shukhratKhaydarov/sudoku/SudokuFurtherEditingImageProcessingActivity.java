package com.shukhratKhaydarov.sudoku;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.shukhratKhaydarov.sudoku.SudokuSolver.SudokuSolverMainActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;


public class SudokuFurtherEditingImageProcessingActivity extends AppCompatActivity {

    public static final int MAX_THRESHOLD=10;

    static List<Mat> chosenListMatWithEmptyCells;
    static List<Integer> chosenListIndex;
    Mat bw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_image_proc);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadModel();

        bindViews(SudokuFurtherEditingImageProcessingActivity.this);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        int number = b.getInt("chosen");
        Mat bw ;
        if(number==1){
            chosenListMatWithEmptyCells=SudokuImageProcessingActivity.resultWithEmptyCells1BinInvTrue;
            chosenListIndex=SudokuImageProcessingActivity.indexesAvailableValues1binInvTrue;
            bw=SudokuImageProcessingActivity.resultWithEmptyCells1BinInvTrue.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues1binInvTrue.get(0));

        }
        else if(number==2){
            chosenListMatWithEmptyCells=SudokuImageProcessingActivity.resultWithEmptyCells1binInvFalse;
            chosenListIndex=SudokuImageProcessingActivity.indexesAvailableValues1binInvFalse;
            bw=SudokuImageProcessingActivity.resultWithEmptyCells1binInvFalse.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues1binInvFalse.get(0));

        }
        else if(number==3){
            chosenListMatWithEmptyCells=SudokuImageProcessingActivity.resultWithEmptyCells2BinInvTrue;
            chosenListIndex=SudokuImageProcessingActivity.indexesAvailableValues2binInvTrue;
            bw=SudokuImageProcessingActivity.resultWithEmptyCells2BinInvTrue.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues2binInvTrue.get(0));

        }
        else{
            chosenListMatWithEmptyCells=SudokuImageProcessingActivity.resultWithEmptyCells2binInvFalse;
            chosenListIndex=SudokuImageProcessingActivity.indexesAvailableValues2binInvFalse;
            bw=SudokuImageProcessingActivity.resultWithEmptyCells2binInvFalse.get(SudokuImageProcessingActivity.indexesOnlyAvailableValues2binInvFalse.get(0));
        }

        Bitmap img_bitmap = Bitmap.createBitmap(bw.cols(), bw.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw, img_bitmap);
        ImageView imageView = findViewById(R.id.img1);
        imageView.setImageBitmap(img_bitmap);

        prevThres=new ArrayList<>();
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        prevThres.add(bw.clone());
        for(int n=0;n<MAX_THRESHOLD;n++) {

            prevThres.add(findBlurs(bw.clone(),n));
        }
        adjustParams(bw,false);
        OpenCVLoader.initDebug();
    }
    private Button zoomPlusButton,minusZoomButton, plusThresButton,minusThresButton,
            plusErodeButton,minusErodeButton, plusDilateButton, minusDilateButton,
            identifyButton, yesButton, noButton;

    private TextView txt_zoom,txt_thres,txt_erode,txt_dilate;
    public int zoomLevelVar,thresholdVar,erodeVar=1, dilateVar=1;



    private void bindViews(Context mContext) {
        zoomPlusButton = findViewById(R.id.plusZoomButton);
        minusZoomButton = findViewById(R.id.minusZoomButton);
        plusThresButton = findViewById(R.id.plusThresButton);
        minusThresButton = findViewById(R.id.minusThresButton);

        plusErodeButton= findViewById(R.id.plusErodeButton);
        minusErodeButton=findViewById(R.id.minusErodeButton);
        plusDilateButton=findViewById(R.id.plusDilateButton);
        minusDilateButton= findViewById(R.id.minusDilateButton);

        identifyButton=findViewById(R.id.identifyButton);
        yesButton=findViewById(R.id.yesButton);
        noButton=findViewById(R.id.noButton);

        txt_zoom = (TextView) findViewById(R.id.textviewZoom);
        txt_thres= (TextView) findViewById(R.id.textviewThres);
        txt_erode= (TextView) findViewById(R.id.textviewErode);
        txt_dilate=(TextView) findViewById(R.id.textviewDilate);


        zoomPlusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoomLevelVar++;

                txt_zoom.setText("Zoom: "+zoomLevelVar);

                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                Mat res=adjustParams(colorimg,false);
                res=findImageCenter(res.clone(), zoomLevelVar);

                Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(res, img_bitmap);
                ImageView imageView = findViewById(R.id.img1);
                imageView.setImageBitmap(img_bitmap);
            }
        });

        minusZoomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoomLevelVar--;
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                if(zoomLevelVar<=0){
                    Mat res=adjustParams(colorimg,false);
                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                    zoomLevelVar=0;
                    txt_zoom.setText("Zoom: "+zoomLevelVar);
                }else {


                    Mat res=adjustParams(colorimg,false);
                    res=findImageCenter(res, zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);

                    txt_zoom.setText("Zoom: "+zoomLevelVar);
                }
            }
        });

        plusThresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thresholdVar++;
                Mat res;

                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                if(zoomLevelVar>0){

                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
            }
        });

        minusThresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                thresholdVar--;

                Mat res;
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                if(zoomLevelVar>0){
                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }


            }
        });

        plusErodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat res;
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                erodeVar++;
                if(zoomLevelVar>0){
                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }


            }
        });

        minusErodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                erodeVar--;

                Mat res;
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                if(zoomLevelVar>0){
                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }

            }
        });

        plusDilateButton.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {
                Mat res;

                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                dilateVar++;
                if(zoomLevelVar>0){
                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }


            }
        });

        minusDilateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dilateVar--;
                Mat res;
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                if(zoomLevelVar>0){
                    res=adjustParams(colorimg,false);
                    res=findImageCenter(res.clone(), zoomLevelVar);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
                else{
                    res=adjustParams(colorimg,false);

                    Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, img_bitmap);
                    ImageView imageView = findViewById(R.id.img1);
                    imageView.setImageBitmap(img_bitmap);
                }
            }
        });

        RelativeLayout ll = (RelativeLayout) findViewById(R.id.topSection);
        final FrameLayout frameLayoutIdentifyButton = (FrameLayout) ll.findViewById(R.id.frameLayoutIdentifyButton);
        final RelativeLayout infoPrediction=(RelativeLayout) ll.findViewById(R.id.infoPrediction);

        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView myImage = findViewById(R.id.img1);
                Mat colorimg = new Mat();

                BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                Utils.bitmapToMat(bitmap, colorimg);

                Mat res=adjustParams(colorimg,false);
                if(zoomLevelVar>0)
                    res=findImageCenter(res, zoomLevelVar);

                Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(res, img_bitmap);
                ImageView imageView = findViewById(R.id.img1);
                imageView.setImageBitmap(img_bitmap);

                int identifiedNumber=onClassify(res);


                frameLayoutIdentifyButton.setVisibility(View.INVISIBLE);

                infoPrediction.setVisibility(View.VISIBLE);
                /*LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewFrameLayout = inflater.inflate(R.layout.activity_editing_image_proc, null);

                FrameLayout item = (FrameLayout ) viewFrameLayout.findViewById(R.id.frameLayoutIdentifyButton);
                item.setVisibility(View.INVISIBLE);*/
                TextView textView=findViewById(R.id.textIdentified);
                textView.setText("Is on image "+identifiedNumber+"?");
            }});
        List<Integer> result = Lists.newArrayList();


        yesButton.setOnClickListener(new View.OnClickListener() {
            int test=0;
            @Override
            public void onClick(View view) {
                /*for(int n=0;n<81;n++){
                    Mat index=chosenListMatWithEmptyCells.get(n);
                    if(!index.empty()){
                        if(test==1) {

                            Mat mat = index.clone();
                            Mat res = adjustParams(mat,true);
                            res = findImageCenter(res, zoomLevelVar);

                            Bitmap img_bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat, img_bitmap);
                            ImageView imageView = findViewById(R.id.img1);
                            imageView.setImageBitmap(img_bitmap);


                        }
                        test++;
                    }

                }*/
                int identifiedNumber;

                for (int n : chosenListIndex) {
                    if(n!=-100) {
                        Mat mat = chosenListMatWithEmptyCells.get(n).clone();
                        //Mat res=mat;
                        Mat res = adjustParams(mat, true);
                        if (zoomLevelVar > 0)
                            res = findImageCenter(res, zoomLevelVar);
                        try {
                            /*Bitmap img_bitmap = Bitmap.createBitmap(res.cols(), res.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(res, img_bitmap);
                            ImageView imageView = findViewById(R.id.img1);
                            imageView.setImageBitmap(img_bitmap);
*/
                            identifiedNumber = onClassify(res);
                            System.out.println("Identified: "+identifiedNumber);
                            //break;
                        }catch (Exception e){
                            System.out.println(n);
                            Mat mat2 = chosenListMatWithEmptyCells.get(n).clone();
                            Bitmap img_bitmap = Bitmap.createBitmap(mat2.cols(), mat2.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat2, img_bitmap);
                            ImageView imageView = findViewById(R.id.img1);
                            imageView.setImageBitmap(img_bitmap);

                            break;
                        }


                        result.add(identifiedNumber);

                        /*prevThres=new ArrayList<>();
                        prevThres.add(res.clone());
                        for(int in=0;in<MAX_THRESHOLD;in++) {

                            prevThres.add(findBlurs(res.clone(),in));
                        }

                        bw=SudokuImageProcessingActivity.resultWithEmptyCells1.get(SudokuImageProcessingActivity.indexesAvailableValues1.get(1));
                       */ //bw=res.clone();
                        //frameLayoutIdentifyButton.setVisibility(View.VISIBLE);

                        //infoPrediction.setVisibility(View.INVISIBLE);
                        //break;
                    }
                    else{
                        result.add(0);
                    }
                }
                Board b = Board.of(9, Joiner.on(" ").join(result));

                String FILENAME = "result.txt";

                String fullPath = save(b.toString(), FILENAME);
                Intent intent = new Intent(SudokuFurtherEditingImageProcessingActivity.this, SudokuSolverMainActivity.class);
                intent.putExtra("image-path", fullPath);
                startActivity(intent);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameLayoutIdentifyButton.setVisibility(View.VISIBLE);
                infoPrediction.setVisibility(View.INVISIBLE);
            }
        });
    }


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
            // 100 means no compression, the lower you go, the stronger the compression

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


    private Integer onClassify(Mat img) {
        Bitmap original=Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(img,original);
        Bitmap scaled = Bitmap.createScaledBitmap(original, 28, 28, false);


        int width = 28;
        int[] pixels = new int[width * width];

        scaled.getPixels(pixels, 0, width, 0, 0, width, width);
        float[] retPixels = createInputPixels(pixels);

        //int[] previewPixels = createPixelsPreview(pixels, retPixels);

        //Bitmap preview = Bitmap.createBitmap(previewPixels, width, width, Bitmap.Config.ARGB_8888);
        //ImageView imageView = findViewById(R.id.img);
        //imageView.setImageBitmap(preview);
        return  classifyData(retPixels);

    }
    private Classifier classifier;
    private Integer classifyData(float[] retPixels) {
        Classification classification = classifier.recognize(retPixels);
        //String result = String.format("It's a %s with confidence: %f", classification.getLabel(), classification.getConf());
        //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        return Integer.parseInt(classification.getLabel());
    }

    private int[] createPixelsPreview(int[] pixels, float[] retPixels) {
        int[] again = new int[pixels.length];
        for (int a = 0; a < pixels.length; a++) {
            again[a] = ColorConverter.tfToPixel(retPixels[a]);
        }
        return again;
    }

    private float[] createInputPixels(int[] pixels) {
        float[] normalized = ColorConverter.convertToTfFormat(pixels);
        return normalized;
    }

    private Executor executor = Executors.newSingleThreadExecutor();

    // tensorflow input and output
    private static final int INPUT_SIZE = 28;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/expert-graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";


    private void loadModel() {
        executor.execute(() -> {
            try {
                classifier = Classifier.create(getApplicationContext().getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        INPUT_NAME,
                        OUTPUT_NAME);
            } catch (final Exception e) {
                throw new RuntimeException("Error initializing TensorFlow!", e);
            }
        });
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent intent = new Intent(SudokuFurtherEditingImageProcessingActivity.this, MainActivity.class);

            startActivity(intent);
            return true;
        }
        if (id == R.id.hint) {
            Intent intent = new Intent(SudokuFurtherEditingImageProcessingActivity.this, Pop.class);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Mat findBlurs(Mat inParam,int thres){
        Mat out=new Mat();
        Mat in=inParam.clone();
        Mat blur=new Mat();
        Imgproc.GaussianBlur(inParam, inParam, new Size(3,3), 0);
        Imgproc.threshold(inParam, out, thres, 255, Imgproc.THRESH_BINARY);
        return out;
    }


    List<Mat> prevThres;
    int prevThresVal=0;
    private Mat adjustParams(Mat cell,boolean updateThresList){
        //Core.bitwise_not(cell,cell);

        //Imgproc.GaussianBlur(cell, cell, new Size(3,3), 0);
        Size sizeEroded,sizeDilate;
        if(thresholdVar<=0) {
            thresholdVar=0;
            minusThresButton.setEnabled(false);
        }
        else if(thresholdVar>= MAX_THRESHOLD){
            plusThresButton.setEnabled(false);
        }
        else{
            plusThresButton.setEnabled(true);
            minusThresButton.setEnabled(true);
        }

        if(erodeVar>=6) {
            plusErodeButton.setEnabled(false);

            sizeEroded=new Size(7,7);
        }
        else if(erodeVar<=1){

            minusErodeButton.setEnabled(false);
            sizeEroded=new Size(1,1);
        }
        else{
            minusErodeButton.setEnabled(true);
            plusErodeButton.setEnabled(true);
            sizeEroded=new Size(erodeVar,erodeVar);
        }

        if(dilateVar>=6){

            plusDilateButton.setEnabled(false);
            sizeDilate=new Size(7,7);
        }
        else if(dilateVar<=1){

            minusDilateButton.setEnabled(false);
            sizeDilate=new Size(1,1);
        }else{
            minusDilateButton.setEnabled(true);
            plusDilateButton.setEnabled(true);
            sizeDilate=new Size(dilateVar,dilateVar);
        }
        Mat thre ;
        if(updateThresList==false) {
            thre = prevThres.get(thresholdVar).clone();
            //Imgproc.threshold(blur, thre, 0, 255, Imgproc.THRESH_BINARY); //5-50
        }
        else{
            prevThres=new ArrayList<>();
            prevThres.add(cell.clone());
            for(int n=0;n<MAX_THRESHOLD;n++) {

                prevThres.add(findBlurs(cell.clone(),n));
            }
            thre = prevThres.get(thresholdVar).clone();
            //thre= prevThres.get(thresholdVar);
        }
        System.out.println("Threshold: "+ thresholdVar);
        System.out.println("Prev threhold: " +prevThresVal);
        txt_thres.setText("Threshold: "+thresholdVar);
        prevThresVal=thresholdVar;
        Mat eroded = new Mat();
        Mat dilated=new Mat();
        try{
            Imgproc.erodeVar(thre, eroded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, sizeEroded));
            System.out.println("Size eroded:"+ sizeEroded);
            txt_erode.setText("Erode: "+sizeEroded);

            Imgproc.dilateVar(eroded, dilated, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, sizeDilate));
            System.out.println("Size dilate:"+ sizeDilate);
            txt_dilate.setText("Dilate: "+sizeDilate);

        }
        catch (Exception e){
            System.out.println("Too much");
        }

        return dilated;
    }



    private Mat findImageCenter(Mat image2,int zoom){
        // reading image
        if(zoom<=0){
            zoom=0;
        }
        Mat image = image2.clone();
        // clone the image
        Mat original = image2.clone();

        double[] centers = {(double)image.width()/2, (double)image.height()/2};
        Point image_center = new Point(centers);

        // finding the contours
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        // finding best bounding rectangle for a contour whose distance is closer to the image center that other ones
        double d_min = Double.MAX_VALUE;
        Rect rect_min = new Rect();
        for (MatOfPoint contour : contours) {
            Rect rec = Imgproc.boundingRect(contour);
            // find the best candidates
            if (rec.height > image.height()/2 & rec.width > image.width()/2)
                continue;
            Point pt1 = new Point((double)rec.x, (double)rec.y);
            Point center = new Point(rec.x+(double)(rec.width)/2, rec.y + (double)(rec.height)/2);
            double d = Math.sqrt(Math.pow((double)(pt1.x-image_center.x),2) + Math.pow((double)(pt1.y -image_center.y), 2));
            if (d < d_min)
            {
                d_min = d;
                rect_min = rec;
            }
        }

        // slicing the image for result region
        int pad = zoom;
        rect_min.x = rect_min.x - pad;
        rect_min.y = rect_min.y - pad;
        System.out.println("Zoom "+zoom);

        rect_min.width = rect_min.width + 2 * pad;
        rect_min.height = rect_min.height + 2 * pad;
        int moreW=rect_min.x+rect_min.width;
        int moreL=rect_min.height+rect_min.y;
        if(moreW>original.width() || moreL>original.height() || rect_min.x==-pad || rect_min.y==-pad) {
            rect_min = new Rect(new Point((original.width()/2)-original.width()/3, (original.height()/2)-original.height()/3), new Size(original.width()/2+original.width()/4,original.height()/2+original.height()/4));
            System.out.println("Rect in mid");
        }

        Mat result =new Mat();
        try {
            result = original.submat(rect_min);

            return result;
        }catch (Exception e){
            Toast.makeText(this,"Sorry, but "+zoom+" is too much, please go lower than this",Toast.LENGTH_LONG);
            System.out.println("Too much");
        }
        return result;
    }

}


