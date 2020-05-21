package com.nz.radar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.nz.radar.SudokuSolver.SudokuSolverMainActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import static com.nz.radar.FeatureDetector.CONTAIN_DIGIT_SUB_MATRIX_DENSITY;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;


public class SudokuImageProcessingActivity extends AppCompatActivity {
    Mat colorimg=null;
    Uri imageUri;
    ImageView myImage;

    CircularProgressButton circularProgressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkPermission();
        circularProgressButton=findViewById(R.id.btnScan);
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<String,String,String> scanImage=new AsyncTask<String,String,String>() {
                    @Override
                    protected String doInBackground(String... voids) {
                        doImageProcessing();
                        return "done";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if(s.equals("done")){
                            Toast.makeText(SudokuImageProcessingActivity.this,"Sudoku Scan done",Toast.LENGTH_LONG).show();
                            circularProgressButton.doneLoadingAnimation(Color.parseColor("#333639"), BitmapFactory.decodeResource(getResources(),R.drawable.ic_done_white_48dp));

                        }
                    }
                };
                circularProgressButton.startAnimation();
                scanImage.execute();
            }
        });

        myImage = findViewById(R.id.img);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        if (intent.hasExtra("image-uri")) {
            imageUri = Uri.parse(intent.getStringExtra("image-uri"));
            /*File file=new File(Environment.getExternalStorageDirectory().getPath(), "photo.jpg");
            boolean e=file.exists();
            Uri uri = Uri.fromFile(file);*/
            myImage.setImageURI(imageUri);
        }
        else{
            Bitmap color=(Bitmap) getIntent().getExtras().get("photo");
            /*byte[] byteArray = getIntent().getByteArrayExtra("photo");
            Bitmap bmp32 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            colorimg=new Mat();
            Utils.bitmapToMat(bmp32, colorimg);*/
            myImage.setImageBitmap(color);
        }

        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        OpenCVLoader.initDebug();
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
            Intent intent = new Intent(SudokuImageProcessingActivity.this, MainActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doImageProcessing() {

        if(colorimg==null) {
            colorimg = new Mat();

            BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            Utils.bitmapToMat(bitmap, colorimg);
        }
        /*Mat bw = getSudokuArea(colorimg);

        Bitmap img_bitmap = Bitmap.createBitmap(bw.cols(), bw.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw, img_bitmap);
        ImageView imageView = findViewById(R.id.img);
        imageView.setImageBitmap(img_bitmap);*/
        List<Integer> res=extractDigits(colorimg);
        Board b = Board.of(9, Joiner.on(" ").join(res));

        String FILENAME = "result.txt";

        String fullPath=save(b.toString(),FILENAME);
        //Toast.makeText(this,b.toString(),Toast.LENGTH_LONG);

        Intent intent = new Intent(SudokuImageProcessingActivity.this, SudokuSolverMainActivity.class);
        intent.putExtra("image-path", fullPath);
        startActivity(intent);

    }
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;

    public void checkPermission(){

                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                //Location

                //Write external Storage
                case 3:
                    break;
                //Read External Storage
                case 4:
                    Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(imageIntent, 11);
                    break;
                //Camera
                case 5:
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, 12);
                    }
                    break;

            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 110: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }*/

    public String save(String text,String FILE_NAME) {
        //checkPermission();
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

    private int elementType = Imgproc.CV_SHAPE_RECT;
    private int kernelSize = 0;

    public Mat preprocess(Mat colorimg) {

        Mat bw = new Mat();

        Imgproc.cvtColor(colorimg, bw, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(bw, bw, new Size(11, 11), 0);

        adaptiveThreshold(bw, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
        //bitwise_not(bw, bw);
        //Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                //new Point(kernelSize, kernelSize));
        //dilate(bw, bw, element);
        return bw;
    }
    private Size FOUR_CORNERS = new Size(1, 4);

    private MatOfPoint2f aproxPolygon(MatOfPoint poly) {

        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f src = new MatOfPoint2f();
        poly.convertTo(src, CvType.CV_32FC2);

        double arcLength = Imgproc.arcLength(src, true);
        approxPolyDP(src, dst, 0.02 * arcLength, true);
        return dst;
    }

    private int distance(MatOfPoint2f poly) {
        Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
                (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }
    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }

    private Mat wrapPerspective(int size, MatOfPoint2f src, Mat image) {
        Size reshape = new Size(size, size);

        Mat undistorted = new Mat(reshape, CvType.CV_8UC1);

        MatOfPoint2f d = new MatOfPoint2f();
        d.fromArray(new Point(0, 0), new Point(0, reshape.width), new Point(reshape.height, 0),
                new Point(reshape.width, reshape.height));

        warpPerspective(image, undistorted, getPerspectiveTransform(src, d), reshape);

        return undistorted;
    }

    private static final Ordering<Point> SORT = Ordering.natural().nullsFirst().onResultOf(
            new Function<Point, Integer>() {
                public Integer apply(Point foo) {
                    return (int) (foo.x+foo.y);
                }
            }
    );

    private MatOfPoint2f orderPoints(MatOfPoint2f mat) {
        List<Point> pointList = SORT.sortedCopy(mat.toList());

        if (pointList.get(1).x > pointList.get(2).x) {
            Collections.swap(pointList, 1, 2);
        }

        MatOfPoint2f s = new MatOfPoint2f();
        s.fromList(pointList);

        return s;
    }

    private Mat preprocess2(Mat image) {
        Mat bw = preprocess(image);
//        erode(bw, bw, crossKernel);

        return bw;
    }

    private Mat cleanLines(Mat image) {
        Mat m = image.clone();
        Mat lines = new Mat();

        int threshold = 50;
        int minLineSize = 200;
        int lineGap = 20;

        Imgproc.HoughLinesP(m, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

        for (int x = 0; x < lines.cols(); x++) {
            double[] vec = lines.get(0, x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);

            Imgproc.line(m, start, end, Scalar.all(0), 3);

        }
        return m;
    }
    public List<Integer> extractDigits(Mat m) {
        Mat sudoku = getSudokuArea(m);
        String t=sudoku.dump();
        if (sudoku == null) {
            return null;
        }

        return extractCells(sudoku);
    }
    private List<Mat> getCells(Mat m) {
        int size = m.height() / 9;

        Size cellSize = new Size(size, size);
        List<Mat> cells = Lists.newArrayList();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Rect rect = new Rect(new Point(col * size, row * size), cellSize);
                Mat digit = new Mat(m, rect).clone();
                cells.add(digit);
                /*try {
                    Mat digit = new Mat(m, rect).clone();
                    cells.add(digit);
                }
                catch (CvException e){

                    rect = new Rect(new Point(col * 84, row * 84), new Size(84, 84));
                    Mat digit = new Mat(m, rect).clone();
                    cells.add(digit);
                    Log.d("E","e");
                }*/
            }
        }

        return cells;
    }


    private List<Integer> extractCells(Mat m) {
        DetectDigit detect = null;
        try {
            Mat img = Utils.loadResource(this, R.drawable.digits, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            detect = new DetectDigit(img);
            //detect = new DetectDigit(Utils.loadResource(getApplicationContext(),R.drawable.digits));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Mat> cells = getCells(m);
        List<Optional<Rect>> digitBoxes = Lists.transform(cells, FeatureDetector.GET_DIGIT_BOX_BYTE_SUM);

        List<Integer> result = Lists.newArrayList();
        List<Mat> cuts = Lists.newArrayList();
        /* zip... zip! :'( */

        for(int i = 0; i < cells.size(); i++ ) {
            Mat cell = cells.get(i);
            //String v=cell.dump();
            com.google.common.base.Optional<Rect> box = digitBoxes.get(i);

            int d = 0;

            if (box.isPresent() && CONTAIN_DIGIT_SUB_MATRIX_DENSITY.apply(cell)) {
                /* cut current cell to the finded box */
                Mat cutted = new Mat(cell, box.get()).clone();
                //String list=cutted.dump();
                Imgproc.rectangle(cell, box.get().tl(), box.get().br(), Scalar.all(255));
                cuts.add(cutted);
                d = detect.detect(cutted);
            }

            Imgproc.rectangle(cell, new Point(0,0), new Point(100,100), Scalar.all(255));

            result.add(d);

        }


        Mat m2 = new Mat(0, cells.get(0).cols(), CvType.CV_8SC1);

        for(Mat digit: cells) {
            m2.push_back(digit.clone());
        }

        //Imgcodecs.imwrite("cells_boxed.jpg", m2);

        return result;
    }

    public Mat getSudokuArea(Mat image) {
        Mat preprocessed = preprocess(image);
        MatOfPoint poly = findBiggerPolygon(preprocessed);
        MatOfPoint2f aproxPoly = aproxPolygon(poly);
//
        if (Objects.equals(aproxPoly.size(), FOUR_CORNERS)) {
            int size = distance(aproxPoly);

            Mat cutted = applyMask(image, poly);

            Mat wrapped = wrapPerspective(size, orderPoints(aproxPoly), cutted);
            Mat preprocessed2 = preprocess2(wrapped);
            Mat withOutLines = cleanLines(preprocessed2);

            return withOutLines;
        }

        return preprocessed;
    }
    public static final Function<MatOfPoint, Integer> AREA = new Function<MatOfPoint, Integer>() {
        @Override
        public Integer apply(MatOfPoint input) {
            return (int)contourArea(input);
        }
    };

        public static final Ordering<MatOfPoint> ORDERING_BY_AREA = Ordering.natural().onResultOf(AREA);

    private MatOfPoint findBiggerPolygon(Mat image) {
        List<MatOfPoint> contours = Lists.newArrayList();
        Mat hierarchy = new Mat();

        findContours(image.clone(), contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return new MatOfPoint();
        }

        MatOfPoint max = ORDERING_BY_AREA.max(contours);

        return max;
    }

}
