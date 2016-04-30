package com.shenfeng.fastdeliver.zhongkongcup;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.widget.Toast;

public class BTActivity extends AppCompatActivity {

    public static final String TAG = "BTActivity";

    private boolean mPhotoShooted;
    private String mFileName;
    /*--------------------------------------------------*/

    private Button enableBtn;
    private Button disableBtn;
    private Button connectionBtn;
    private Button sendMsgButton;
    private TextView showWindowTextView;
    private EditText sendMsgEditText;
    private ImageView mImageView;
    private TextView mBlockColorTextView;

    private Button openCameraBtn;
    private Button calibBtn;

    private static final String mRemoteAddress = "20:15:04:21:03:80"; // 蓝牙串口模块的地址
    private BluetoothSocket mbtSocket = null;
    public static OutputStream mOutputStream = null;
    public static InputStream mInputStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");   //蓝牙串口服务的UUID

    //Request BT enabled
//    private static final int REQUEST_ENABLE = 0x1;
    //Request BT discover
    private static final int REQUEST_DISCOVERABLE = 0x2;

    //    private String rcvdata;
    private BluetoothAdapter mBluetoothAdapter;
    public static Bitmap bottles_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        enableBtn = (Button) findViewById(R.id.enableBtn);
        disableBtn = (Button) findViewById(R.id.disableBtn);
        connectionBtn = (Button) findViewById(R.id.connect_button);
        sendMsgButton = (Button) findViewById(R.id.sendMsg_button);
        showWindowTextView = (TextView) findViewById(R.id.MsgTextView);
        sendMsgEditText = (EditText) findViewById(R.id.sendMsgEditText);
        openCameraBtn = (Button) findViewById(R.id.openCamera_button);
        calibBtn = (Button) findViewById(R.id.calibBtn);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mBlockColorTextView = (TextView) findViewById(R.id.blockColorTextView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BTActivity.this, MainActivity.class);
//                startActivityForResult(i, REQUEST_TAKE_PHOTO);
                startActivity(i);
            }
        });

        //标定按钮设置监听器
        calibBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BTActivity.this, CameraCalibrationActivity.class);
                startActivity(i);
            }
        });
        showWindowTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.enable();
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(enabler, REQUEST_DISCOVERABLE);
            }
        });

        disableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.disable();
            }
        });
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Editable message = sendMsgEditText.getText();
                    for (int i = 0; i < message.length(); i++) {
                        if (mOutputStream != null) {
                            mOutputStream.write(message.charAt(i));
                            Log.i(TAG, "write msg to remote device.Value: " + message.charAt(i));
                        }
                    }
                    showWindowTextView.append("->" + message + "\n");

                } catch (IOException e) {
                    Log.e(TAG, "raise an IOException");
                }

            }
        });
        connectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(BTActivity.this, "Bluetooth can't be discovered", Toast.LENGTH_LONG).show();
                }
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mRemoteAddress);
                try {
                    mbtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    Log.e(TAG, "Create RFCOMM socket to device ERROR!", e);
                }

                mBluetoothAdapter.cancelDiscovery();
                try {
                    mbtSocket.connect();
                } catch (IOException e) {
                    Toast.makeText(BTActivity.this, "Socket connect error.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mOutputStream = mbtSocket.getOutputStream();
                    mInputStream = mbtSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mFileName == null)
                    return;
                try {
                    FileInputStream fis = openFileInput(mFileName);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    mImageView.setImageBitmap(bitmap);
                    fis.close();
                    Log.i(TAG, "open goods picture success");

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "open goods picture error");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "read goods picture error");
                    e.printStackTrace();
                }
            }
        });


//        text0.setText("Bluetooth discover!");

        if (mBluetoothAdapter == null) {
            Toast.makeText(BTActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }

        /*******************蓝牙模块结束**********************/
    }

    @Override
    protected void onStart() {
        super.onStart();
        //测试打开图片文件
//        showImage(mFileName);
        Log.i(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());

    }

    @Override
    protected void onStop() {
        super.onStop();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mbtSocket != null) {
            try {
                mbtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close the socket");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
//        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            mFileName = data.getStringExtra(ZhongKongCameraFragment.EXTRA_PHOTO_NAME);
//            mPhotoShooted = data.getBooleanExtra(ZhongKongCameraFragment.EXTRA_PHOTO_IS_SHOOTED, false);
//            if (mPhotoShooted) {
//                Log.i(TAG, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                .getAbsolutePath());
//                Mat originImage = Imgcodecs.imread(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES).getAbsolutePath() + mFileName, 1);
//
//
//                Mat dst = new Mat();
//                Mat edge = new Mat();
//                Mat gray = new Mat();
//                dst.create(originImage.size(), originImage.type());
////                Imgproc.cvtColor(originImage, gray, Imgproc.COLOR_BGR2GRAY);
//
////                Imgproc.Canny(originImage, originImage, 150, 100);
////                Imgcodecs.imwrite(mFileName, originImage);
//                showImage();


//                Log.i(TAG, "depth is:" + rgbImage.depth());
//                Log.i(TAG, "type is:" + rgbImage.type());
//                Log.i(TAG, "channel is:" + rgbImage.channels());

//                judgeBlockColor(rgbImage);

//                if (BTrecvMsgThread.currentState == Status.ImageToBeProcessed_STATE) {
//                    if (current_good == GoodKind.GOOD_SQUARE_YELLOW) {
//                        judgeBlockColor(originImage);
//                    } else if (current_good == GoodKind.GOOD_MEINIANDA) {
//
//                    } else if (current_good == GoodKind.GOOD_YANGLEDUO) {
//
//                    }
//
//                    currentState = Status.INITIAL_STATE;
//                } else if (currentState == Status.POSITIONING_STATE) {
//                    //TODO
//                    currentState = Status.INITIAL_STATE;
//                }
//            }
//        }
    }


}
