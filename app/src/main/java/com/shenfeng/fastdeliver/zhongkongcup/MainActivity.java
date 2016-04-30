package com.shenfeng.fastdeliver.zhongkongcup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    public static final String TAG = "MainActivity";

    public static final String EXTRA_TO_SHOOT_ONE_PHOTO = "com.shenfeng.fastdeliver.zhongkongcup.toshootonephoto";
    public static final String featureDetectTAG = "FDlog";
    private static final float FOCALX = 1036;
    private static final float FOCALY = 538;
//    private static final int REQUEST_TAKE_PHOTO = 0x6;

    private BTconnector mBTconnector;
    public static MyCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    //BlobColorDetect constants
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;
    public static ColorBlobDetector mBlobDetector;
    public static ORBfeatureDetector sORBfeatureDetector;
    private boolean mIsColorSelected = false;

    //TODO 测试参数， 需要修改
    private static final Scalar YELLOSHSV = new Scalar(33, 150, 170);
    private static final Scalar REDHSV = new Scalar(235, 150, 140);
    private static final Scalar GREENHSV = new Scalar(100, 200, 140);
    private static final Scalar BLUEHSV = new Scalar(157, 190, 140);
    private static final Scalar GRAYHSV = new Scalar(145, 20, 150);

    static Mat mat_meiNianDa;
    static Mat mat_fengDa;
    static Mat mat_jianYi;
    static Mat mat_baiShi;
    static Mat mat_xueBi;
    static Mat mat_xueHua;
    static Mat mat_yangLeDuo;
    static Mat mat_youJun;
    static Mat mat_youYiC;
    static Mat mat_qqStar;
    static Mat mat_smallqqStar1;
    static Mat mat_smallqqStar2;

    static Mat mat_mengNiuPure;
    static Mat mat_mengNiuGaoGai;
    static Mat mat_yiLi;

    public static InputStream mBTInputStream = null;
    public static OutputStream mBTOutputStream = null;

    private boolean startFeatureDetect = false;
    private boolean mIsAdjustingFocal = false;
    //Menu Item
    private MenuItem mGetHSV;
    private MenuItem mAdjustFocal;
    private MenuItem mAutoAdjustFocal;
    private MenuItem mFourPhotoMode;
    private MenuItem mItemYellowBlock;
    private MenuItem mItemRedBlock;
    private MenuItem mItemGreenBlock;
    private MenuItem mItemBlueBlock;
    private MenuItem mItemMeiNianDa;
    private MenuItem mItemJianYi;
    private MenuItem mItemXueBi;
    private MenuItem mItemDarea;
    public static Mat mRGBA;
    private boolean mIsGettingHSV = false;
    private boolean FourPhotoModel = false;

    private boolean isYangLeDuoDetected = false;
    private boolean isMengNiuPureDetected = false;
    private boolean isQQstarDetected = false;
    private boolean isDetectingBlock = false;
    public static Mat mRGBA_leftUp;
    public static Mat mRGBA_leftBtm;
    public static Mat mRGBA_rightUp;
    public static Mat mRGBA_rightBtm;

    //Detect Mode
    enum DetectMode {
        DetectYellowBlock,
        DetectRedBlock,
        DetectGreenBlock,
        DetectBlueBlock,
        DetectMeiNianDa,
        DetectJianYi,
        DetectXueBi,
        DetectDarea,
    }

    public static DetectMode sDetectMode = DetectMode.DetectYellowBlock;

    public static Handler mMainHandler = new Handler() {

        public void prepareForTakePhoto()
        {
            mOpenCvCameraView.autoAdjustFocalOnFour();
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int region = 0;
            MainActivity.mOpenCvCameraView.setFlashMode(null, MyCameraView.FLASHON); //flash mode on
            try {
                if (msg.arg1 == BTrecvMsgThread.MSG_IS_YELLOW_SQUARE) {
                    mBlobDetector.setHsvColor(YELLOSHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize yellow block");
                    region = ProcessFourBlock("Yellow", false);
                    mBTOutputStream.write(region);
                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_RED_SQUARE) {
                    mBlobDetector.setHsvColor(REDHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize red block");
                    mBTOutputStream.write(ProcessFourBlock("Red", true));
                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_GREEN_SQUARE) {
                    mBlobDetector.setHsvColor(GREENHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize green block");
                    mBTOutputStream.write(ProcessFourBlock("Green", false));
                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_BLUE_SQUARE) {
                    mBlobDetector.setHsvColor(BLUEHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize blue block");
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));
                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_MEINIANDA) {
//                    BTrecvMsgThread.current_good = BTrecvMsgThread.GoodKind.GOOD_MEINIANDA;
//                    BTrecvMsgThread.currentState = BTrecvMsgThread.Status.ImageToBeProcessed_STATE;
                    mBlobDetector.setHsvColor(BLUEHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize meinianda");
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));
                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_JIANYI) {
//                    BTrecvMsgThread.current_good = BTrecvMsgThread.GoodKind.GOOD_JIANYI;
//                    BTrecvMsgThread.currentState = BTrecvMsgThread.Status.ImageToBeProcessed_STATE;

                    mBlobDetector.setHsvColor(BLUEHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize jianyi");
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));

                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_XUEBI) {
                    BTrecvMsgThread.current_good = BTrecvMsgThread.GoodKind.GOOD_XUEBI;
                    BTrecvMsgThread.currentState = BTrecvMsgThread.Status.ImageToBeProcessed_STATE;

                    mBlobDetector.setHsvColor(BLUEHSV);
                    prepareForTakePhoto();
                    Log.i(MainActivity.TAG, "Begin to recognize xuebi");
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));

                } else if (msg.arg1 == BTrecvMsgThread.MSG_IS_D_AREA) {
                    BTrecvMsgThread.current_good = BTrecvMsgThread.GoodKind.GOOD_D_AREA;
                    BTrecvMsgThread.currentState = BTrecvMsgThread.Status.ImageToBeProcessed_STATE;

                }
                mOpenCvCameraView.setFlashMode(null, MyCameraView.FLASHOFF); //flash mode off

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.surface_view);

        mOpenCvCameraView = (MyCameraView) findViewById(R.id.camera_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);


        mBTconnector = new BTconnector();
        mBTconnector.enableBT();
        if (mBTconnector.connectBT()) {
            Toast.makeText(this, "connect BlueTooth successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "connect BlueTooth succcessfully");
        } else {
            Toast.makeText(this, "connect BlueTooth failed", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "connect BlueTooth failed");
        }
        mBTInputStream = mBTconnector.getInputStream();
        mBTOutputStream = mBTconnector.getOutputStream();
        new Thread(new BTrecvMsgThread(mBTconnector)).start();
        new Thread(new BTreconnect()).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mBlobDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

        sORBfeatureDetector = new ORBfeatureDetector();

        mat_xueBi = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/xuebi_tpl.jpg");
        mat_xueHua = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/xuehua_tpl.jpg");
        mat_meiNianDa = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/meinianda_tpl.jpg");
        mat_fengDa = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/fengda_tpl.jpg");
        mat_qqStar = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/qqstar_b_tpl.jpg");
        mat_smallqqStar1 = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/qqstar_s1_tpl.jpg");
        mat_smallqqStar2 = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/qqstar_s2_tpl.jpg");
//        mat_mengNiuPure = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
//        + "/")
        mat_yangLeDuo = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/yangleduo_tpl.jpg");
        mat_youYiC = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/youyiC_tpl.jpg");
        mat_youJun = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/youjun_tpl.jpg");
        mat_yiLi = Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/yili_tpl.jpg");

//        Camera.Size resolution = mOpenCvCameraView.getResolutionList().get(2); //
//        mOpenCvCameraView.setResolution(resolution);
        mOpenCvCameraView.setFocusMode(this, 0); //auto focus
        mOpenCvCameraView.setFlashMode(this, MyCameraView.FLASHOFF); //flash mode off

//        for(int i = 0; i < mOpenCvCameraView.getResolutionList().size(); i++) {
//            Log.i(TAG, "width:" + mOpenCvCameraView.getResolutionList().get(i).width
//                    + " height: " + mOpenCvCameraView.getResolutionList().get(i).height);
//        }
        mOpenCvCameraView.autoFocus();
    }

    public void onCameraViewStopped() {
    }

    //提高相机像素
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBA_leftUp = new Mat(mRGBA, new Rect(0, 0, mRGBA.cols() / 2, mRGBA.rows() / 2 + 100));
        mRGBA_leftBtm = new Mat(mRGBA, new Rect(0, mRGBA.rows() / 2 + 100, mRGBA.cols() / 2, mRGBA.rows() / 2 - 100));
        mRGBA_rightUp = new Mat(mRGBA, new Rect(mRGBA.cols() / 2, 0, mRGBA.cols() / 2, mRGBA.rows() / 2 + 100));
        mRGBA_rightBtm = new Mat(mRGBA, new Rect(mRGBA.cols() / 2, mRGBA.rows() / 2 + 100, mRGBA.cols() / 2, mRGBA.rows() / 2 - 100));

        if (startFeatureDetect) {
//            if(!FourPhotoModel) { //不是四图模式
            if (isDetectingBlock) {
                if (sDetectMode == DetectMode.DetectYellowBlock) {
                    mBlobDetector.setHsvColor(YELLOSHSV);
                    prepareForTakePhoto();

                    ProcessFourBlock("Yellow", false);
                } else if (sDetectMode == DetectMode.DetectRedBlock) {
                    mBlobDetector.setHsvColor(REDHSV);
                    prepareForTakePhoto();
                    ProcessFourBlock("Red", true);
                } else if (sDetectMode == DetectMode.DetectGreenBlock) {
                    mBlobDetector.setHsvColor(GREENHSV);
                    prepareForTakePhoto();
                    ProcessFourBlock("Green", false);
                } else if (sDetectMode == DetectMode.DetectBlueBlock) {
                    mBlobDetector.setHsvColor(BLUEHSV);
                    prepareForTakePhoto();
                    ProcessFourBlock("Blue", false);
                }

                isDetectingBlock = false;
            } else if (sDetectMode == DetectMode.DetectMeiNianDa) {
                double mnd_min_dist = sORBfeatureDetector.process(mat_meiNianDa, mRGBA);
                double fd_min_dist = sORBfeatureDetector.process(mat_fengDa, mRGBA);

                Log.i(featureDetectTAG, "average dist: 美年达:" + mnd_min_dist + " 芬达:" + fd_min_dist);
                if (mnd_min_dist < fd_min_dist && mnd_min_dist != -1 && fd_min_dist != -1) {
                    Log.i(featureDetectTAG, "检测到美年达");
                } else {
                    Log.i(featureDetectTAG, "检测到芬达");
                }
            } else if (sDetectMode == DetectMode.DetectXueBi) {
//                if (FourPhotoModel) {
//                ProcessFourBottle(mat_xueBi, mat_xueHua, "雪碧");
                mBlobDetector.setHsvColor(BLUEHSV);
                prepareForTakePhoto();
                Log.i(MainActivity.TAG, "Begin to recognize xuebi");
                try {
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                } else {
//                    double xuebi_avr_dist = sORBfeatureDetector.process(mat_xueBi, mRGBA);
//                    double xuehua_avr_dist = sORBfeatureDetector.process(mat_xueHua, mRGBA);
//                    Log.i(featureDetectTAG, "average dist 雪碧:" + xuebi_avr_dist + " 雪花:" + xuehua_avr_dist);
//                    if (xuebi_avr_dist < xuehua_avr_dist && xuebi_avr_dist != -1 && xuehua_avr_dist != -1) {
//                        Log.i(featureDetectTAG, "检测到雪碧");
//                    } else {
//                        Log.i(featureDetectTAG, "检测到雪花");
//                    }
//                }
            } else if (sDetectMode == DetectMode.DetectJianYi) {

//                double jy_avr_dist = sORBfeatureDetector.process(mat_jianYi, mRGBA);
//                double bs_arv_dist = sORBfeatureDetector.process(mat_baiShi, mRGBA);

                mBlobDetector.setHsvColor(BLUEHSV);
                prepareForTakePhoto();
                Log.i(MainActivity.TAG, "Begin to recognize xuebi");
                try {
                    mBTOutputStream.write(ProcessFourBlock("Blue", false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Log.i(featureDetectTAG, "average dist: 健怡:" + jy_avr_dist + " 百事:" + bs_arv_dist);
//                if (jy_avr_dist < bs_arv_dist && jy_avr_dist != -1 && bs_arv_dist != -1) {
//                    Log.i(featureDetectTAG, "检测到健怡");
//                } else {
//                    Log.i(featureDetectTAG, "检测到百事");
//                }
            } else if (sDetectMode == DetectMode.DetectDarea) {
                double[] d_area_dists = new double[9];
                for (int i = 0; i < 9; i++) {
                    d_area_dists[i] = 1000;
                }

                String[] bottleNames = {"qq星", "小qq星小虎", "小qq星小熊", "养乐多", "优菌", "优益C", "蒙牛纯", "伊利", "蒙牛高钙"};

                d_area_dists[0] = sORBfeatureDetector.process(mat_qqStar, mRGBA);
                d_area_dists[1] = sORBfeatureDetector.process(mat_smallqqStar1, mRGBA);
                d_area_dists[2] = sORBfeatureDetector.process(mat_smallqqStar2, mRGBA);
                d_area_dists[3] = sORBfeatureDetector.process(mat_yangLeDuo, mRGBA);
                d_area_dists[4] = sORBfeatureDetector.process(mat_youJun, mRGBA);
                d_area_dists[5] = sORBfeatureDetector.process(mat_youYiC, mRGBA);
//                    d_area_dists[6] = sORBfeatureDetector.process(mat_mengNiuPure, mRGBA);
                d_area_dists[7] = sORBfeatureDetector.process(mat_yiLi, mRGBA);
//                    d_area_dists[8] = sORBfeatureDetector.process(mat_mengNiuGaoGai, mRGBA);
                double min_dist = 500;
                int min_dist_index = -1;
                for (int i = 0; i < 9; i++) {
                    Log.i(featureDetectTAG, "d_area_dists " + bottleNames[i] + ":" + d_area_dists[i]);
                    if (d_area_dists[i] < min_dist) {
                        min_dist = d_area_dists[i];
                        min_dist_index = i;
                    }
                }

                Log.i(featureDetectTAG, "检测到" + bottleNames[min_dist_index] + " min dist:" + min_dist);
            }
            //TODO D 区域检测
            startFeatureDetect = false;
            mOpenCvCameraView.setFlashMode(null, MyCameraView.FLASHOFF); //flash mode off

        }
        return mRGBA;
//            List<MatOfPoint> contours = mBlobDetector.getContours();
//            Log.i(TAG, "Contours count: " + contours.size());
//            Imgproc.drawContours(mRGBA, contours, -1, CONTOUR_COLOR);
//
//            Mat colorLabel = mRGBA.submat(4, 68, 4, 68);
//            colorLabel.setTo(mBlobColorRgba);
//
//            Mat spectrumLabel = mRGBA.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//            mSpectrum.copyTo(spectrumLabel);
//
//            mIsColorSelected = false;
//        }
    }

    public static int ProcessFourBlock(String name, boolean isRedBlock) {
        int region = 0;
        if (name == "Yellow") {
            if (mBlobDetector.process(mRGBA_leftUp, isRedBlock) > ColorBlobDetector.MinAreaOfBlock) {
                Log.d(TAG, name + " Block detected in left up!");
                region += 4;
            }
            if (mBlobDetector.process(mRGBA_leftBtm, isRedBlock) > ColorBlobDetector.MinAreaOfBlock) {
                Log.d(TAG, name + " Block detected in left bottom");
                region += 8;
            }
            if (mBlobDetector.process(mRGBA_rightUp, isRedBlock) > ColorBlobDetector.MinAreaOfBlock) {
                Log.d(TAG, name + " Block detected in right up");
                region += 2;
            }
            if (mBlobDetector.process(mRGBA_rightBtm, isRedBlock) > ColorBlobDetector.MinAreaOfBlock) {
                Log.d(TAG, name + " Block detected in right bottom");
                region += 1;
            }
        } else {
            double maxArea = 0;
            double maxAreaLeftUp = mBlobDetector.process(mRGBA_leftUp, isRedBlock);
            double maxAreaLeftBtm = mBlobDetector.process(mRGBA_leftBtm, isRedBlock);
            double maxAreaRightUp = mBlobDetector.process(mRGBA_rightUp, isRedBlock);
            double maxAreaRightBtm = mBlobDetector.process(mRGBA_rightBtm, isRedBlock);
            if (maxAreaLeftUp > maxArea) maxArea = maxAreaLeftUp;
            if (maxAreaLeftBtm > maxArea) maxArea = maxAreaLeftBtm;
            if (maxAreaRightUp > maxArea) maxArea = maxAreaRightUp;
            if (maxAreaRightBtm > maxArea) maxArea = maxAreaRightBtm;
            if (maxArea == maxAreaLeftUp) {
                Log.d(TAG, name + " Block detected in left up!");
                region += 4;
            }
            else if (maxArea == maxAreaLeftBtm) {
                Log.d(TAG, name + " Block detected in left bottom");
                region += 8;
            }
            else if (maxArea == maxAreaRightUp) {
                Log.d(TAG, name + " Block detected in right up");
                region += 2;
            }
            else if (maxArea == maxAreaRightBtm) {
                Log.d(TAG, name + " Block detected in right bottom");
                region += 1;
            }
        }
        Log.i(TAG, "region value:" + region);
        return region;
    }

    private void ProcessFourBottle(Mat mat_bottle_need, Mat mat_bottle_NoNeed, String name) {
        try {
//            double min_dist_leftUp_need = sORBfeatureDetector.process(mat_bottle_need, mRGBA_leftUp);
//            double min_dist_leftUp_NoNeed = sORBfeatureDetector.process(mat_bottle_NoNeed, mRGBA_leftUp);
//            if(min_dist_leftUp_need < min_dist_leftUp_NoNeed) {
//                Log.i(featureDetectTAG, "左上方检测到" + name);
//                return;
//            }
//
//            double min_dist_leftBtm_need = sORBfeatureDetector.process(mat_bottle_need, mRGBA_leftBtm);
//            double min_dist_leftBtm_NoNeed = sORBfeatureDetector.process(mat_bottle_NoNeed, mRGBA_leftBtm);
//
//            if(min_dist_leftBtm_need < min_dist_leftBtm_NoNeed) {
//                Log.i(featureDetectTAG, "左下方检测到" + name);
//                return;
//            }
//
//            double min_dist_rightUp_need= sORBfeatureDetector.process(mat_bottle_need, mRGBA_rightUp);
//            double min_dist_rightUp_NoNeed= sORBfeatureDetector.process(mat_bottle_NoNeed, mRGBA_rightUp);
//            if(min_dist_rightUp_need < min_dist_rightUp_NoNeed) {
//                Log.i(featureDetectTAG, "右上方检测到" + name);
//                return;
//            }
//
//            double min_dist_rightBtm_need = sORBfeatureDetector.process(mat_bottle_need, mRGBA_rightBtm);
//            double min_dist_rightBtm_NoNeed = sORBfeatureDetector.process(mat_bottle_NoNeed, mRGBA_rightBtm);
//            if(min_dist_rightBtm_need < min_dist_rightBtm_NoNeed) {
//                Log.i(featureDetectTAG, "右下方检测到" + name);
//                return;
//            }
            double min_dist = 10000;
            double min_dist_leftUp = sORBfeatureDetector.process(mRGBA_leftUp, mat_bottle_need);
            double min_dist_leftBtm = sORBfeatureDetector.process(mRGBA_leftBtm, mat_bottle_need);
            double min_dist_rightUp = sORBfeatureDetector.process(mRGBA_rightUp, mat_bottle_need);
            double min_dist_rightBtm = sORBfeatureDetector.process(mRGBA_rightBtm, mat_bottle_need);

            if (min_dist_leftUp < min_dist) min_dist = min_dist_leftUp;
            if (min_dist_leftBtm < min_dist) min_dist = min_dist_leftBtm;
            if (min_dist_rightUp < min_dist) min_dist = min_dist_rightUp;
            if (min_dist_rightBtm < min_dist) min_dist = min_dist_rightBtm;

            if (min_dist == min_dist_leftUp)
                Log.i(featureDetectTAG, "左上方检测到" + name);
            if (min_dist == min_dist_leftBtm)
                Log.i(featureDetectTAG, "左下方检测到" + name);
            if (min_dist == min_dist_rightUp)
                Log.i(featureDetectTAG, "右上方检测到" + name);
            if (min_dist == min_dist_rightBtm)
                Log.i(featureDetectTAG, "右下方检测到" + name);

        } catch (Exception e) {
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (mIsAdjustingFocal) {
            mOpenCvCameraView.focusOnTouch(event);
            return false;
        }
//        startFeatureDetect = true;
        //调试用, 计算触碰区域的HSV值
        if (mIsGettingHSV) {
            int cols = mRGBA.cols();
            int rows = mRGBA.rows();

            int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
            int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;

//            Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

            if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

            Rect touchedRect = new Rect();

            touchedRect.x = (x > 4) ? x - 4 : 0;
            touchedRect.y = (y > 4) ? y - 4 : 0;

            touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
            Mat touchedRegionRgba = mRGBA.submat(touchedRect);
            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            // Calculate average color of touched region
            int pointCount = touchedRect.width * touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++) {
                mBlobColorHsv.val[i] /= pointCount;
            }
            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
//            Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                    ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

            mBlobDetector.setHsvColor(mBlobColorHsv);

            Imgproc.resize(mBlobDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
            touchedRegionRgba.release();
            touchedRegionHsv.release();
            return false;
        }

        if (sDetectMode == DetectMode.DetectDarea || sDetectMode == DetectMode.DetectXueBi || sDetectMode == DetectMode.DetectMeiNianDa
                || sDetectMode == DetectMode.DetectJianYi || sDetectMode == DetectMode.DetectYellowBlock
                || sDetectMode == DetectMode.DetectRedBlock || sDetectMode == DetectMode.DetectGreenBlock
                || sDetectMode == DetectMode.DetectBlueBlock) {
            startFeatureDetect = true;
        }
        if (sDetectMode == DetectMode.DetectRedBlock || sDetectMode == DetectMode.DetectGreenBlock
                || sDetectMode == DetectMode.DetectBlueBlock || sDetectMode == DetectMode.DetectYellowBlock) {
            isDetectingBlock = true;
        }
//        mIsColorSelected = true;

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mGetHSV = menu.add("获取区域HSV");
        mAdjustFocal = menu.add("手动调焦");
        mAutoAdjustFocal = menu.add("自动调焦");
        mItemYellowBlock = menu.add("Yellow Block");
        mItemRedBlock = menu.add("Red Block");
        mItemGreenBlock = menu.add("Green Block");
        mItemBlueBlock = menu.add("Blue Block");
        mItemMeiNianDa = menu.add("美年达");
        mItemJianYi = menu.add("健怡");
        mItemXueBi = menu.add("雪碧");
        mItemDarea = menu.add("D区");
        mFourPhotoMode = menu.add("四图模式");
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemYellowBlock) {
            mBlobDetector.setHsvColor(YELLOSHSV);
            sDetectMode = DetectMode.DetectYellowBlock;
        } else if (item == mItemRedBlock) {
            mBlobDetector.setHsvColor(REDHSV);
            sDetectMode = DetectMode.DetectRedBlock;
        } else if (item == mItemGreenBlock) {
            mBlobDetector.setHsvColor(GREENHSV);
            sDetectMode = DetectMode.DetectGreenBlock;
        } else if (item == mItemBlueBlock) {
            mBlobDetector.setHsvColor(BLUEHSV);
            sDetectMode = DetectMode.DetectBlueBlock;
        } else if (item == mItemMeiNianDa) {
            sDetectMode = DetectMode.DetectMeiNianDa;
        } else if (item == mItemJianYi) {
            sDetectMode = DetectMode.DetectJianYi;
        } else if (item == mItemXueBi) {
            sDetectMode = DetectMode.DetectXueBi;
        } else if (item == mItemDarea) {
            sDetectMode = DetectMode.DetectDarea;
        } else if (item == mGetHSV) {
            if (mIsGettingHSV == false) mIsGettingHSV = true;
            else mIsGettingHSV = false;

        } else if (item == mAdjustFocal) {
            if (mIsAdjustingFocal == false) mIsAdjustingFocal = true;
            else mIsAdjustingFocal = false;

        } else if (item == mFourPhotoMode) {
            if (FourPhotoModel) FourPhotoModel = false;
            else FourPhotoModel = true;

        } else if (item == mAutoAdjustFocal) {
            //识别四张图的时候在四个物体上分别聚焦
            mOpenCvCameraView.autoAdjustFocalOnFour();
        }
        return true;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public void prepareForTakePhoto()
    {
        mOpenCvCameraView.setFlashMode(this, MyCameraView.FLASHON); //flash mode on
        mOpenCvCameraView.autoAdjustFocalOnFour();
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public class BTreconnect implements Runnable{
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mBTconnector.isconnected()) {
                    Log.v(MainActivity.TAG, "BT disconnected!");
                    if(mBTconnector.connectBT()) {
                        Log.i(MainActivity.TAG, "reconnect BT successfully!");
                        mBTInputStream = mBTconnector.getInputStream();
                        mBTOutputStream = mBTconnector.getOutputStream();
                    } else {
                        Log.v(MainActivity.TAG, "reconnect BT failed");
                    }
                }
            }
        }
    }
}
