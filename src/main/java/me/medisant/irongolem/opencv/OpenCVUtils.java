package me.medisant.irongolem.opencv;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;

public class OpenCVUtils {

    public static double getPSNR(Mat img1, Mat img2) {
        try {

            if (img1.empty() || img2.empty()) {
                System.out.println("Images could not be loaded");
                return 100;
            }

            Mat s1 = new Mat();

            Core.absdiff(img1, img2, s1); // |img1 - img2|

            s1.convertTo(s1, 5); // CV_32F
            s1 = s1.mul(s1); // |img1 - img2| ^2

            Scalar s = Core.sumElems(s1); // sum elements per channel

            double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

            if (sse <= 1e-10) return 0;

            double mse = sse / (double) (img1.channels() * img1.total());

            return 10.0 * Math.log10((255 * 255) / mse);

        } catch (CvException e) {
            return 100.0;
        }
    }

    public static double getMSSIM(Mat img1, Mat img2) {
        try {

            if (img1.empty() || img2.empty()) {
                System.out.println("Images could not be loaded");
                return 0.0;
            }

            double c1 = 6.5025, c2 = 58.5225;
            int d = CV_32F; // (5)

            Mat i1 = new Mat(), i2 = new Mat();
            img1.convertTo(i1, d);
            img2.convertTo(i2, d);

            Mat i1_2 = i1.mul(i1); // i1 ^2
            Mat i2_2 = i2.mul(i2); // i2 ^2
            Mat i1_i2 = i1.mul(i2); // i1 * i2

            Mat mu1 = new Mat(), mu2 = new Mat();   // PRELIMINARY COMPUTING
            Imgproc.GaussianBlur(i1, mu1, new Size(11, 11), 1.5);
            Imgproc.GaussianBlur(i2, mu2, new Size(11, 11), 1.5);

            Mat mu1_2 = mu1.mul(mu1);
            Mat mu2_2 = mu2.mul(mu2);
            Mat mu1_mu2 = mu1.mul(mu2);

            Mat sigma1_2 = new Mat(), sigma2_2 = new Mat(), sigma12 = new Mat();
            Imgproc.GaussianBlur(i1_2, sigma1_2, new Size(11, 11), 1.5);
            Core.subtract(sigma1_2, mu1_2, sigma1_2);
            //sigma1_2 = sigma1_2 - mu1_2;
            Imgproc.GaussianBlur(i2_2, sigma2_2, new Size(11, 11), 1.5);
            Core.subtract(sigma2_2, mu2_2, sigma2_2);
            //sigma2_2 = sigma2_2 - mu2_2;
            Imgproc.GaussianBlur(i1_i2, sigma12, new Size(11, 11), 1.5);
            Core.subtract(sigma12, mu1_mu2, sigma12);
            //sigma12 = sigma12 - mu1_mu2;

            Mat t1 = new Mat(), t2 = new Mat(), t3;

            Core.multiply(mu1_mu2, new Scalar(2), t1);
            Core.add(t1, new Scalar(c1), t1);
            //t1 = 2 * mu1_mu2 + c1;

            Core.multiply(sigma12, new Scalar(2), t2);
            Core.add(t2, new Scalar(c2), t2);
            //t2 = 2 * sigma12 + c2;

            t3 = t1.mul(t2);

            //t1 = mu1_2 + mu2_2 + c1;
            Core.add(mu1_2, mu2_2, t1);
            Core.add(t1, new Scalar(c1), t1);

            //t2 = sigma1_2 + sigma2_2 + c2;
            Core.add(sigma1_2, sigma2_2, t2);
            Core.add(t2, new Scalar(c2), t2);

            t1 = t1.mul(t2);

            Mat ssim_map = new Mat();
            Core.divide(t3, t1, ssim_map);

            Scalar mssim = Core.mean(ssim_map);
            return mssim.val[0];

        } catch (CvException e) {
            return 0.0;
        }
    }

    public static double[] compareHistograms(Mat img1, Mat img2) {
        try {

            if (img1.empty() || img2.empty()) {
                System.out.println("Images could not be loaded");
                return new double[]{0.0, 0.0, 0.0};
            }

            // convert to HSV format
            Mat hsvBase = new Mat(), hsvTest1 = new Mat(), hsvTest2 = new Mat();
            Imgproc.cvtColor(img1, hsvBase, Imgproc.COLOR_BGR2HSV);
            Imgproc.cvtColor(img2, hsvTest1, Imgproc.COLOR_BGR2HSV);


            int hBins = 50, sBins = 60;
            int[] histSize = {hBins, sBins};
            // hue varies from 0 to 179, saturation from 0 to 255
            float[] ranges = {0, 180, 0, 256};
            // Use the 0-th and 1-st channels
            int[] channels = {0, 1};

            Mat histBase = new Mat();
            Mat histTest1 = new Mat();

            List<Mat> hsvBaseList = List.of(hsvBase);
            Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), histBase, new MatOfInt(histSize), new MatOfFloat(ranges), false);
            Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX);

            List<Mat> hsvTest1List = List.of(hsvTest1);
            Imgproc.calcHist(hsvTest1List, new MatOfInt(channels), new Mat(), histTest1, new MatOfInt(histSize), new MatOfFloat(ranges), false);
            Core.normalize(histTest1, histTest1, 0, 1, Core.NORM_MINMAX);

            List<Float> output = new ArrayList<>();

            for (int compareMethod = 0; compareMethod < 4; compareMethod++) {
                double baseTest1 = Imgproc.compareHist(histBase, histTest1, compareMethod);
                output.add((float) baseTest1);
            }


            double correlation = output.get(0) * 100f;
            double intersection = 100f * output.get(2) / 1.5249442;
            double bhattacharyya = 100f - output.get(3) * 100f;

            return new double[]{correlation, intersection, bhattacharyya};

        } catch (CvException e) {
            return new double[]{0.0, 0.0, 0.0};
        }
    }

}
