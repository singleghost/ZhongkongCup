package com.shenfeng.fastdeliver.zhongkongcup;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorBlobDetector {
    public static final String TAG = "ColorBlobDetector";
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    private Scalar mMaxBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(20, 70, 70, 0);
    private Mat mSpectrum = new Mat();
    private Scalar hsvColor = new Scalar(0);
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mMask2 = new Mat(); //检测红色方块的时候才用到
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    public static final double MinAreaOfBlock = 100;
    private Mat mDilatedMask2 = new Mat();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        Log.i(MainActivity.TAG, "set the HsvColor " + hsvColor.toString());
        if(hsvColor == MainActivity.YELLOSHSV) {
            Log.i(MainActivity.TAG, "set yellow color radius(20,70,70,0)");
            setColorRadius(new Scalar(20, 70, 50, 0));
        } else {
            Log.i(MainActivity.TAG, "set nonYellow color radius(20,100,100,0)");
            setColorRadius(new Scalar(20, 100, 100, 0));
        }
        this.hsvColor = hsvColor;
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0] - mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 255) ? hsvColor.val[0] + mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH - minH; j++) {
            byte[] tmp = {(byte) (minH + j), (byte) 255, (byte) 255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public double process(Mat rgbaImage, boolean isRedBlock) {
        double maxArea = 0;
        double sumArea = 0;
        double maxArea2 = 0;
        int redCount = 0;

        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        if (isRedBlock) {
            for (int i = 0; i < mHsvMat.height(); i++)
                for (int j = 0; j < mHsvMat.width(); j++) {
                    double[] point;
                    try {
                        point = mHsvMat.get(i, j);
                        if ((point[0] > 245 || point[0] < 10) && point[1] > 50 && point[1] < 250 && point[2] > 40 && point[2] < 220)
                            redCount++;
                    } catch (Exception e) {
                        Log.e(MainActivity.TAG, "get point (" + i + ", " + j + ") error");
                    }

                }
            Log.i(MainActivity.TAG, "redCount: " + redCount);
            return redCount;
        }
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);


        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
            sumArea += area; //红色方块时用到
        }


        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                mContours.add(contour);
            }
        }
        Log.i(MainActivity.TAG, "maxArea:" + maxArea);
        return maxArea;
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}
