package com.shenfeng.fastdeliver.zhongkongcup;

import android.os.Environment;
import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

/**
 * Created by singleghost on 16-2-17.
 */
public class ORBfeatureDetector {
//    static {
//        System.loadLibrary("jniPart");
//    }
    private static final String TAG = "ORBfeatureDetector";
    private static final int NUMOFMATCHES = 30;
    //SURF constants
    Mat mdescriptors_object;
    Mat mdescriptors_scene;
    MatOfKeyPoint mKeyPoints_object;
    MatOfKeyPoint mKeyPoints_scene;
    FeatureDetector mFeatureDetector;
    DescriptorExtractor mDescriptorExtractor;
    DescriptorMatcher mDescriptorMatcher;
    MatOfDMatch matches;
    LinkedList<DMatch> mGoodMatches;
    MatOfDMatch gm;

//    LinkedList<Point> objList = new LinkedList<Point>();
//    LinkedList<Point> sceneList = new LinkedList<Point>();
//    MatOfPoint2f obj = new MatOfPoint2f();
//    MatOfPoint2f scene = new MatOfPoint2f();

    double max_dist;
    double min_dist;
    private Mat objectImgGray;
    private Mat sceneImgGray;
    private Mat resultImg;

    Scalar RED = new Scalar(255, 0,0);
    Scalar GREEN = new Scalar(0, 255, 0);
    private double average_dist = 0.0;
    //surf

    public ORBfeatureDetector() {
    }

    public double process(Mat objectImg, Mat sceneImg) {
        if(objectImg.empty() || sceneImg.empty()) return -1;
        //初始化
        mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);

        mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        mKeyPoints_object = new MatOfKeyPoint();
        mKeyPoints_scene = new MatOfKeyPoint();
        mdescriptors_object = new Mat();
        mdescriptors_scene = new Mat();
        matches = new MatOfDMatch();
        objectImgGray = new Mat();
        sceneImgGray = new Mat();
        resultImg = new Mat();


        mGoodMatches = new LinkedList<DMatch>();
        gm = new MatOfDMatch();

        min_dist = 100;
        max_dist = 0;
        average_dist = 0;

        Imgproc.cvtColor(objectImg, objectImgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(sceneImg, sceneImgGray, Imgproc.COLOR_RGBA2GRAY);
        mFeatureDetector.detect(objectImgGray, mKeyPoints_object);
        mFeatureDetector.detect(sceneImgGray, mKeyPoints_scene);

        mDescriptorExtractor.compute(objectImgGray, mKeyPoints_object, mdescriptors_object);
        mDescriptorExtractor.compute(sceneImgGray, mKeyPoints_scene, mdescriptors_scene);

//        train_desc_collection = new LinkedList<>();
//        matches_list = new LinkedList<>();
//        train_desc_collection.add(mdescriptors_object);
        mDescriptorMatcher.match(mdescriptors_object, mdescriptors_scene, matches);
//        mDescriptorMatcher.add(train_desc_collection);
//        mDescriptorMatcher.train();
//        mDescriptorMatcher.knnMatch(mdescriptors_scene, matches_list,2);

        List<DMatch> matchesList = matches.toList();

//        for(int i = 0; i < matches_list.size(); i++) {
//            if (matches_list.get(i).toList().get(0).distance <
//                    0.6 * matches_list.get(i).toList().get(1).distance) {
//                mGoodMatches.addLast(matches_list.get(i).toList().get(0));
//            }
//        }
        double dist;
        double [] dists_list = new double[500];
        for(int i = 0; i < 500; i++) dists_list[i] = 10000;

        for(int i = 0; i < mdescriptors_object.rows(); i++) {
            try {
                dist = (double) matchesList.get(i).distance;
                dists_list[i] = dist;

                average_dist += dist;
                if (dist < min_dist) min_dist = dist;
                if (dist > max_dist) max_dist = dist;
            } catch(Exception e) {
                break;
            }
        }
        Arrays.sort(dists_list);
        double avr_dist_part = average_part(dists_list, NUMOFMATCHES);
        gm.fromList(mGoodMatches);
        
        if(mdescriptors_object.rows() != 0) {
            average_dist = average_dist / mdescriptors_object.rows();
        }
        Log.i(TAG, "average dist:" + average_dist + ". avr dist of top"
        + NUMOFMATCHES + ": " + avr_dist_part + ".min dist:" + min_dist);
//        Log.i(MainActivity.featureDetectTAG, "min dist:" + min_dist + "max dist:" + max_dist);

//        for(int i = 0; i < mdescriptors_object.rows(); i++) {
//            try {
//                if (matchesList.get(i).distance < 2 * min_dist)
//                    mGoodMatches.addLast(matchesList.get(i));
//            } catch (Exception e) {
//                break;
//            }
//        }


//        resultImg = new Mat();
//        MatOfByte drawnMatches = new MatOfByte();
//        Features2d.drawMatches(sceneImg, mKeyPoints_scene, objectImg, mKeyPoints_object,
//                gm, resultImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
//        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/39matches"
//                        + UUID.randomUUID().toString() + ".jpg", resultImg);
//        Log.i(MainActivity.featureDetectTAG, "good matches: " + mGoodMatches.size());
//        if(mGoodMatches.size() > 20) {
            //TODO
        return min_dist;
//        } else {
//            return false;
//        }

        //perspective transform

//        List<KeyPoint> keypoints_objectList = mKeyPoints_object.toList();
//        List<KeyPoint> keypoints_sceneList = mKeyPoints_scene.toList();
//
//        for(int i = 0; i < mGoodMatches.size(); i++) {
//            objList.addLast(keypoints_objectList.get(mGoodMatches.get(i).queryIdx).pt);
//            sceneList.addLast(keypoints_sceneList.get(mGoodMatches.get(i).trainIdx).pt);
//        }
//
//        obj.fromList(objList);
//        scene.fromList(sceneList);
//
//        Mat H = Calib3d.findHomography(obj, scene);
    }

    private double average_part(double[] dists_list, int num) {
        double total = 0;
        for(int i = 0; i < num; i++) {
            total += dists_list[i];
        }
        return (total / num);
    }

    public Mat getDrawMatchImg() {
        return resultImg;
    }
}
