package com.nz.radar;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;

import static org.opencv.imgproc.Imgproc.moments;
import static org.opencv.imgproc.Imgproc.warpAffine;

//import org.opencv.highgui.Highgui;


public class DetectDigit {


    //public static final String DIGITS = "/digits.png";
    public static final int SZ = 20;

    private KNearest knn;

    public DetectDigit(Mat img) {
        init(img);
    }

    private void init(Mat img) {

        // Converting the image to gray scale and
        // saving it in the dst matrix
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);

        Size cellSize = new Size(SZ, SZ);

        //Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);

        int cols = img.width() / 20;
        int rows = img.height() / 20;

        int totalPerClass = cols * rows / 10;

        Mat samples = Mat.zeros(cols * rows, SZ * SZ, CvType.CV_32FC1);
        Mat labels = Mat.zeros(cols * rows, 1, CvType.CV_32FC1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                Rect rect = new Rect(new Point(j * SZ, i * SZ), cellSize);

                int currentCell = i * cols+j;
                double label = (j+i * cols) / totalPerClass;

                // HACK!
                if (label == 0) continue;

                Mat cell = deskew(new Mat(img, rect));
                Mat procCell = procSimple(cell);

                for (int k = 0; k < SZ * SZ; k++) {
                    samples.put(currentCell, k, procCell.get(0, k));
                }

                labels.put(currentCell, 0, label);
            }
        }

        knn = KNearest.create();
        knn.train(samples, Ml.ROW_SAMPLE,labels);
    }

    public Mat deskew(Mat img) {
        Moments m = moments(img);

        if (Math.abs(m.get_mu02()) < 0.01) {
            return img.clone();
        }
        Mat result = new Mat(img.size(), CvType.CV_32FC1);
        double skew = m.get_mu11() / m.get_mu02();
        Mat M = new Mat(2, 3, CvType.CV_32FC1);

        M.put(0, 0, 1, skew, -0.5 * SZ * skew, 0, 1, 0);

        warpAffine(img, result, M, new Size(SZ, SZ), Imgproc.WARP_INVERSE_MAP | Imgproc.INTER_LINEAR);

        return result;
    }

    private Mat center(Mat digit) {
        Mat res = Mat.zeros(digit.size(), CvType.CV_32FC1);

        double s = 1.5*digit.height()/SZ;

        Moments m = moments(digit);
        double c1_0 = m.get_m10() / m.get_m00();
        double c1_1 = m.get_m01() / m.get_m00();
        /*if(m.get_m00()==0){
             c1_0=1.0;
             c1_1=1.0;
        }else {
             c1_0 = m.get_m10() / m.get_m00();
             c1_1 = m.get_m01() / m.get_m00();
        }*/
        double c0_0= SZ/2, c0_1 = SZ/2;

        double t_0 = c1_0 - s*c0_0;
        double t_1 = c1_1 - s*c0_1;

        Mat A = Mat.zeros( new Size(3, 2), CvType.CV_32FC1);

        A.put(0,0, s, 0, t_0);
        A.put(1,0, 0, s, t_1);
        warpAffine(digit, res, A, new Size(SZ, SZ), Imgproc.WARP_INVERSE_MAP | Imgproc.INTER_LINEAR);
        /*try {

            warpAffine(digit, res, A, new Size(SZ, SZ), Imgproc.WARP_INVERSE_MAP | Imgproc.INTER_LINEAR);
        }catch (Exception e){
            Log.d("e
            ","e");
        }*/

        return res;
    }


    public Mat procSimple(Mat img) {
        Mat result = Mat.zeros(1, SZ * SZ, CvType.CV_32FC1);

        for (int row = 0; row < img.rows(); row++) {
            for (int col = 0; col < img.cols(); col++) {
                int nro = SZ * row+col;
                double value = img.get(row, col)[0] / 255.0;
                result.put(0, nro, value);
            }
        }

        return result;
    }

    public Integer detect(Mat digit) {
        Mat wraped = deskew(center(digit.clone()));
        Mat result = new Mat();
        Mat neighborhood = new Mat();
        Mat distances = new Mat();

        knn.findNearest(procSimple(wraped), 3, result, neighborhood, distances);

        System.out.printf("%s\n", neighborhood.dump());

        return (int)result.get(0,0)[0];
    }
}