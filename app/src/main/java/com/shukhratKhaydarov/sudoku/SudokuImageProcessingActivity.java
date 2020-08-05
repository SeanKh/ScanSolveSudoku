package com.shukhratKhaydarov.sudoku;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import static com.shukhratKhaydarov.sudoku.FeatureDetector.CONTAIN_DIGIT_SUB_MATRIX_DENSITY;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;


public class SudokuImageProcessingActivity extends AppCompatActivity {
    Mat colorimg=null;
    Uri imageUri;
    ImageView myImage;
    CircularProgressButton circularProgressButton;
    private Size FOUR_CORNERS = new Size(1, 4);
    static List<Integer>  indexesAvailableValues1binInvTrue,indexesAvailableValues1binInvFalse,
            indexesAvailableValues2binInvTrue,indexesAvailableValues2binInvFalse,
            indexesOnlyAvailableValues1binInvTrue,indexesOnlyAvailableValues1binInvFalse,
            indexesOnlyAvailableValues2binInvTrue,indexesOnlyAvailableValues2binInvFalse;

    static List<Mat> resultWithEmptyCells1BinInvTrue,resultWithEmptyCells1binInvFalse,
            resultWithEmptyCells2BinInvTrue,resultWithEmptyCells2binInvFalse ;

    /**
     * Preprocesses the color image
     * @param  colorimg  color image
     * @param  firstWay  first time preprocessing
     * @return      the preprocessed image
     */
    public Mat preprocess(Mat colorimg, boolean firstWay) {
        if(firstWay==true) {
            Mat bw = new Mat();
            Imgproc.cvtColor(colorimg, bw, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(bw, bw, new Size(11, 11), 0);
            Imgproc.adaptiveThreshold(bw, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
            return bw;
        }
        else{
            Mat sudoku = new Mat();
            Mat outerBox=new Mat();
            Imgproc.cvtColor(colorimg, sudoku, Imgproc.COLOR_RGB2GRAY);
            //Imgproc.GaussianBlur(sudoku, sudoku, new Size(11, 11), 0);
            Imgproc.GaussianBlur(sudoku, sudoku, new Size(5, 5), 0);

            Imgproc.adaptiveThreshold(sudoku, outerBox, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
            bitwise_not(outerBox, outerBox);

            byte [] data = {0,1,0,1,1,1,0,1,0};
            Mat ukernel = new Mat(3,3, CvType.CV_8U);
            ukernel.put(0,0,data);
            Imgproc.dilateVar(outerBox, outerBox, ukernel);

            return outerBox;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        circularProgressButton=findViewById(R.id.btnScan);
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<String,String,String> scanImage=new AsyncTask<String,String,String>() {
                    @Override
                    protected String doInBackground(String... voids) {
                        forwardToFurtherIP();
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

        Intent intent = getIntent();
        if (intent.hasExtra("image-uri")) {
            imageUri = Uri.parse(intent.getStringExtra("image-uri"));
            myImage.setImageURI(imageUri);
        }
        else{
            Bitmap color=(Bitmap) getIntent().getExtras().get("photo");
            myImage.setImageBitmap(color);
        }
        OpenCVLoader.initDebug();

    }



    public void forwardToFurtherIP(){
        colorimg = new Mat();

        BitmapDrawable drawable = (BitmapDrawable) myImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Utils.bitmapToMat(bitmap, colorimg);

        extractDigits(colorimg,true);

        extractDigits(colorimg,false);


        Intent intent = new Intent(this, SudokuFurtherImageProcessingActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent intent = new Intent(SudokuImageProcessingActivity.this, MainActivity.class);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets cells from the image matrix
     * @param  image   matrix of the image
     * @return      the list of extracted cells
     */
    private List<Mat> getCells(Mat image) {
        int size = image.height() / 9;
        Size cellSize = new Size(size, size);
        List<Mat> cells = Lists.newArrayList();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Rect rect = new Rect(new Point(col * size, row * size), cellSize);
                try {
                    Mat digit = new Mat(image, rect).clone();
                    cells.add(digit);
                }catch (Exception e){
                    Log.d("E","e");
                }
            }
        }
        return cells;
    }

    /**
     * Extracts digits from the image matrix
     * @param  m   matrix of the image
     * @return      the list of extracted digits
     */
    public void extractDigits(Mat m,boolean way) {
        Mat sudoku = getSudokuArea(m,way);

        if (sudoku == null) {
            return;
        }

        extractCells(sudoku,way);
    }

    /**
     * Approximates a polygonal curves with the specified precision
     * @param  poly   matrix points for the image
     * @return      the approximated polygonal curve matrix of image
     */
    private MatOfPoint2f aproxPolygon(MatOfPoint poly) {
        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f src = new MatOfPoint2f();
        poly.convertTo(src, CvType.CV_32FC2);

        double arcLength = Imgproc.arcLength(src, true);
        approxPolyDP(src, dst, 0.02 * arcLength, true);
        return dst;
    }

    /**
     * Finds distance of points from supplied matrix points
     * @param  poly   matrix points for the image
     * @return      the distance of points
     */
    private int distance(MatOfPoint2f poly) {
        Point[] a =  poly.toArray();
        return (int)Math.sqrt((a[0].x - a[1].x)*(a[0].x - a[1].x) +
                (a[0].y - a[1].y)*(a[0].y - a[1].y));
    }

    /**
     * Applies mask on image matrix
     * @param  image   matrix of the image
     * @param  poly   matrix points for the image
     * @return      the masked matrix of image
     */
    private Mat applyMask(Mat image, MatOfPoint poly) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);

        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(255), -1);
        Imgproc.drawContours(mask, ImmutableList.of(poly), 0, Scalar.all(0), 2);

        Mat dst = new Mat();
        image.copyTo(dst, mask);

        return dst;
    }

    /**
     * Extracts sudoku area from image
     * @param  image   matrix of the image
     * @param  way  first time preprocessing
     * @return      the sudoku area extracted from image
     */
    public Mat getSudokuArea(Mat image,boolean way) {
        Mat preprocessed = preprocess(image,way);
        MatOfPoint poly = findBiggerPolygon(preprocessed);
        MatOfPoint2f aproxPoly = aproxPolygon(poly);

        if (Objects.equals(aproxPoly.size(), FOUR_CORNERS)) {
            int size = distance(aproxPoly);

            Mat cutted = applyMask(image, poly);

            Mat wrapped = wrapPerspective(size, orderPoints(aproxPoly), cutted);
            Mat preprocessed2 = preprocess(wrapped,way);
            Mat withOutLines = cleanLines(preprocessed2);

            return withOutLines;
        }

        return preprocessed;
    }

    /**
     * Applies a perspective transformation to an image
     * @param  image   matrix of the image
     * @param  size   size for reshaping
     * @param  src   source image
     * @return      the masked matrix of image
     */
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

    /**
     * Sorts the point in image matrix
     * @param  mat   matrix of the image points
     * @return      the sorted matrix of image points
     */
    private MatOfPoint2f orderPoints(MatOfPoint2f mat) {
        List<Point> pointList = SORT.sortedCopy(mat.toList());

        if (pointList.get(1).x > pointList.get(2).x) {
            Collections.swap(pointList, 1, 2);
        }

        MatOfPoint2f s = new MatOfPoint2f();
        s.fromList(pointList);

        return s;
    }

    /**
     * Cleans lines in the image
     * @param  image   matrix of the image
     * @return      the image with sharper lines
     */
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

    public static final Function<MatOfPoint, Integer> AREA = new Function<MatOfPoint, Integer>() {
        @Override
        public Integer apply(MatOfPoint input) {
            return (int)contourArea(input);
        }
    };

    public static final Ordering<MatOfPoint> ORDERING_BY_AREA = Ordering.natural().onResultOf(AREA);

    /**
     * Finds biggest polygon from image
     * @param  image   matrix of the image
     * @return      the matrix point of found biggest polygon
     */
    private MatOfPoint findBiggerPolygon(Mat image) {
        List<MatOfPoint> contours = Lists.newArrayList();
        Mat hierarchy = new Mat();

        Imgproc.findContours(image.clone(), contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return new MatOfPoint();
        }

        MatOfPoint max = ORDERING_BY_AREA.max(contours);
        return max;
    }

    /**
     * Extracts cells from the image matrix
     * @param  m   matrix of the image
     * @param  way  first time preprocessing
     */
    private void extractCells(Mat m,boolean way) {
        List<Mat> cells = getCells(m);
        List<Optional<Rect>> digitBoxes = Lists.transform(cells, FeatureDetector.GET_DIGIT_BOX_BYTE_SUM);

        if(way==true) {
            resultWithEmptyCells1BinInvTrue = Lists.newArrayList();
            indexesAvailableValues1binInvTrue= Lists.newArrayList();
            indexesAvailableValues1binInvFalse= Lists.newArrayList();

            resultWithEmptyCells1binInvFalse= Lists.newArrayList();
            indexesOnlyAvailableValues1binInvTrue= Lists.newArrayList();
            indexesOnlyAvailableValues1binInvFalse= Lists.newArrayList();
        }
        else {
            resultWithEmptyCells2BinInvTrue = Lists.newArrayList();
            indexesAvailableValues2binInvTrue= Lists.newArrayList();
            indexesAvailableValues2binInvFalse= Lists.newArrayList();

            resultWithEmptyCells2binInvFalse= Lists.newArrayList();
            indexesOnlyAvailableValues2binInvTrue= Lists.newArrayList();
            indexesOnlyAvailableValues2binInvFalse= Lists.newArrayList();
        }
        for(int i = 0; i < cells.size(); i++ ) {
            Mat cell = cells.get(i).clone();
            com.google.common.base.Optional<Rect> box = digitBoxes.get(i);
            if (box.isPresent() && CONTAIN_DIGIT_SUB_MATRIX_DENSITY.apply(cell)) {
                bitwise_not(cell,cell);

                //result.add(cell);
                if(way==true){
                    Mat cellCopy=cell.clone();
                    resultWithEmptyCells1binInvFalse.add(findImageCenter(cellCopy,false));
                    indexesAvailableValues1binInvFalse.add(i);
                    indexesOnlyAvailableValues1binInvFalse.add(i);

                    resultWithEmptyCells1BinInvTrue.add(findImageCenter(cell.clone(),true));
                    //resultWithEmptyCells1BinInvTrue.add(cell);
                    indexesAvailableValues1binInvTrue.add(i);
                    indexesOnlyAvailableValues1binInvTrue.add(i);
                }
                else{
                    Mat cellCopy=cell.clone();
                    resultWithEmptyCells2BinInvTrue.add(findImageCenter(cell.clone(),true));
                    indexesAvailableValues2binInvTrue.add(i);
                    indexesOnlyAvailableValues2binInvTrue.add(i);

                    resultWithEmptyCells2binInvFalse.add(findImageCenter(cellCopy,false));
                    indexesAvailableValues2binInvFalse.add(i);
                    indexesOnlyAvailableValues2binInvFalse.add(i);
                }
            }
            else {
                if (way == true) {
                    resultWithEmptyCells1BinInvTrue.add(new Mat());
                    indexesAvailableValues1binInvTrue.add(-100);
                    resultWithEmptyCells1binInvFalse.add(new Mat());
                    indexesAvailableValues1binInvFalse.add(-100);
                } else {
                    resultWithEmptyCells2BinInvTrue.add(new Mat());
                    indexesAvailableValues2binInvTrue.add(-100);
                    resultWithEmptyCells2binInvFalse.add(new Mat());
                    indexesAvailableValues2binInvFalse.add(-100);
                }
            }
        }
        //return result;
    }

    private Mat findImageCenter(Mat image2, boolean binInvTrue){
        // reading image
        Mat image = image2;
        // clone the image
        Mat original = image.clone();
        if(binInvTrue==true) {
            // thresholding the image to make a binary image
            Imgproc.threshold(image, image, 10, 128, Imgproc.THRESH_BINARY_INV);
        }
        else{
            Imgproc.threshold(image, image, 10, 128, Imgproc.THRESH_BINARY);
        }
        // find the center of the image
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
        Mat result=new Mat();
        // slicing the image for result region
        int pad = 1;
        rect_min.x = rect_min.x - pad;
        rect_min.y = rect_min.y - pad;

        rect_min.width = rect_min.width + 2 * pad;
        rect_min.height = rect_min.height + 2 * pad;
        int moreW=rect_min.x+rect_min.width;
        int moreL=rect_min.height+rect_min.y;
        if(moreW>original.width() || moreL>original.height() || rect_min.x==-pad || rect_min.y==-pad) {
            rect_min = new Rect(new Point((original.width()/2)-original.width()/3, (original.height()/2)-original.height()/3), new Size(original.width()/2+original.width()/4,original.height()/2+original.height()/4));
        }

        try {
            result = original.submat(rect_min);
        }catch (Exception e){
            Bitmap img_bitmap = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(image, img_bitmap);

            ImageView imageView = findViewById(R.id.img);
            imageView.setImageBitmap(img_bitmap);
        }
        //result=original;
        return result;
    }
}



