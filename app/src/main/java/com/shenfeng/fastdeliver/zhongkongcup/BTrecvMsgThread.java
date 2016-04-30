package com.shenfeng.fastdeliver.zhongkongcup;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by singleghost on 16-2-16.
 */
class BTrecvMsgThread implements Runnable {
    private static final String TAG = "BTrecvMsgThread";
    static Status currentState = Status.INITIAL_STATE;
    static GoodKind current_good;
    public static BTconnector btConn;

    enum Status {
        INITIAL_STATE,
        //        Rcv_GOOD_KIND_FROM_BT_STATE,
        ImageToBeProcessed_STATE,
//        POSITIONING_STATE
    }

    enum GoodKind {
        GOOD_SQUARE_YELLOW,
        GOOD_SQUARE_RED,
        GOOD_SQUARE_GREEN,
        GOOD_SQUARE_BLUE,
        GOOD_MEINIANDA,
        GOOD_JIANYI,
        GOOD_XUEBI,
        GOOD_D_AREA
    }

    /*定义各种消息类型*/
    public final static int MSG_SHOOT_PHOTO = 0x1;
    public final static int MSG_IS_YELLOW_SQUARE = 0x2;
    public final static int MSG_IS_RED_SQUARE = 0x3;
    public final static int MSG_IS_GREEN_SQUARE = 0x4;
    public final static int MSG_IS_BLUE_SQUARE = 0x5;
    public final static int MSG_IS_MEINIANDA = 0x6;
    public final static int MSG_IS_JIANYI = 0x7;
    public final static int MSG_IS_XUEBI = 0x8;
    public final static int MSG_IS_D_AREA = 0x9;
    public final static int MSG_POSITIONING = 0x10;
    public final static int MSG_IS_IN_CENTRE = 0x11;

    private InputStream istream;
    private Message msg = new Message();

    //    public BTrecvMsgThread(InputStream istream) {
//        this.istream = istream;
//    }
    public BTrecvMsgThread(BTconnector btConnector) {
        btConn = btConnector;
        Log.i(MainActivity.TAG, "BTrecvMsgThread init");
    }

    @Override
    public void run() {
        Log.i(MainActivity.TAG, "BTrecvMsgThread run");
        char c;
//        try {
//            while(istream.available() > 0) { istream.read(); }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        while (true) {
            istream = MainActivity.mBTInputStream;
            if (istream == null) continue;
            try {
                if (istream.available() > 0) {
                    c = (char) istream.read();
                    Log.d(MainActivity.TAG, "rcvdata is:" + c);
//                    if (rcvdata.charAt(0) == 'y') {
                    if (c == 'y') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_YELLOW_SQUARE, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);
                    } //else if (rcvdata.charAt(0) == 'r') {
                    else if (c == 'r') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_RED_SQUARE, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);

                    } else if (c == 'g') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_GREEN_SQUARE, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);

                    } else if (c == 'b') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_BLUE_SQUARE, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);

                    } else if (c == '1') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_MEINIANDA, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);
                    } else if (c == '2') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_JIANYI, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);
                    } else if (c == '3') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_XUEBI, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);
                    } else if (c == '4') {
                        msg = MainActivity.mMainHandler.obtainMessage(MSG_SHOOT_PHOTO, MSG_IS_D_AREA, 1, c);
                        MainActivity.mMainHandler.sendMessage(msg);
                    }
                }
        }catch(IOException e){
            continue;
        }
    }

}
}
