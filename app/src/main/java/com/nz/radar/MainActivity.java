package com.nz.radar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.nz.radar.FeatureDetector.CONTAIN_DIGIT_SUB_MATRIX_DENSITY;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
    }

    public void displayToast(View v) {
        Mat colorimg = null;

        try {
            colorimg = Utils.loadResource(getApplicationContext(), R.drawable.sudoku2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat bw = getSudokuArea(colorimg);
        Bitmap img_bitmap = Bitmap.createBitmap(bw.cols(), bw.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bw, img_bitmap);
        ImageView imageView = findViewById(R.id.img);
        imageView.setImageBitmap(img_bitmap);
        List<Integer> res=extractDigits(colorimg);
        Board b = Board.of(9, Joiner.on(" ").join(res));

        String FILENAME = "result.txt";
        String string = "Grabbed sudoku\n==============\n\n";

        save(b.toString(),FILENAME);
        Toast.makeText(this,b.toString(),Toast.LENGTH_LONG);
    }

    public void checkPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        110);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
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
    }

    public void save(String text,String FILE_NAME) {
        checkPermission();
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
            Toast.makeText(this, "Saved to " + fullPath + "/" + FILE_NAME,
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
    }

    private int elementType = Imgproc.CV_SHAPE_RECT;
    private int kernelSize = 0;
    public Mat preprocess(Mat colorimg) {

        Mat bw = new Mat();

        Imgproc.cvtColor(colorimg, bw, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(bw, bw, new Size(11, 11), 0);

        adaptiveThreshold(bw, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
        //bitwise_not(bw, bw);
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        dilate(bw, bw, element);
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
            }
        }

        return cells;
    }


    private List<Integer> extractCells(Mat m) {
        DetectDigit detect = null;
        try {
            detect = new DetectDigit(Utils.loadResource(getApplicationContext(),R.drawable.digits));
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
            com.google.common.base.Optional<Rect> box = digitBoxes.get(i);

            int d = 0;

            if (box.isPresent() && CONTAIN_DIGIT_SUB_MATRIX_DENSITY.apply(cell)) {
                /* cut current cell to the finded box */
                Mat cutted = new Mat(cell, box.get()).clone();
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

        Imgcodecs.imwrite("cells_boxed.jpg", m2);

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
