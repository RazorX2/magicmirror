/*
 * =========================================================================
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * =========================================================================
 * @file LiveRecognition.java
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LiveRecognition extends Activity implements Camera.PreviewCallback {

    private static PREVIEW_ROTATION_ANGLE rotationAngle = PREVIEW_ROTATION_ANGLE.ROT_90;
    Camera cameraObj; // Accessing the Android native Camera.
    FrameLayout preview;
    CameraSurfacePreview mPreview;
    private int FRONT_CAMERA_INDEX = 1;
    private int BACK_CAMERA_INDEX = 0;
    private OrientationEventListener orientationListener;
    private FacialProcessing faceObj;
    private int frameWidth;
    private int frameHeight;
    private boolean cameraFacingFront = true;
    private DrawView drawView;
    private FaceData[] faceArray; // Array in which all the face data values will be returned for each face detected.
    private ImageView switchCameraButton;
    private Vibrator vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recognition);
        final TextView Date = (TextView) findViewById(R.id.Date);
        final TextView Time = (TextView) findViewById(R.id.Time);
        Date.setText(getCurrentDate());
        Time.setText(getCurrentTime());
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Date.setText(getCurrentDate());
                                Time.setText(getCurrentTime());
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        faceObj = FacialRecognitionActivity.faceObj;
        vibrate = (Vibrator) LiveRecognition.this
                .getSystemService(Context.VIBRATOR_SERVICE);

        orientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {

            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.live_recognition, menu);
        return true;
    }

    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        if (cameraObj != null) {
            stopCamera();
        }
        startCamera();
    }

    /*
     * Stops the camera preview. Releases the camera. Make the objects null.
     */
    private void stopCamera() {

        if (cameraObj != null) {
            cameraObj.stopPreview();
            cameraObj.setPreviewCallback(null);
            preview.removeView(mPreview);
            cameraObj.release();
        }
        cameraObj = null;
    }

    /*
     * Method that handles initialization and starting of camera.
     */
    private void startCamera() {
        if (cameraFacingFront) {
            cameraObj = Camera.open(FRONT_CAMERA_INDEX); // Open the Front camera
        } else {
            cameraObj = Camera.open(BACK_CAMERA_INDEX); // Open the back camera
        }
        mPreview = new CameraSurfacePreview(LiveRecognition.this, cameraObj,
                orientationListener); // Create a new surface on which Camera will be displayed.

        cameraObj.setPreviewCallback(LiveRecognition.this);
        frameWidth = cameraObj.getParameters().getPreviewSize().width;
        frameHeight = cameraObj.getParameters().getPreviewSize().height;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.i("TAG","Made it to recognition part");
        boolean result = false;
        faceObj.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        if (cameraFacingFront) {
            result = faceObj.setFrame(data, frameWidth, frameHeight, true,
                    rotationAngle);
        } else {
            result = faceObj.setFrame(data, frameWidth, frameHeight, false,
                    rotationAngle);
        }
        if (result) {
            int numFaces = faceObj.getNumFaces();
            if (numFaces == 0) {
                Log.d("TAG", "No Face Detected");
                if (drawView != null) {
                    preview.removeView(drawView);
                    drawView = new DrawView(this, null, false);
                    preview.addView(drawView);
                }
            } else {
                faceArray = faceObj.getFaceData();
                if (faceArray == null) {
                    Log.e("TAG", "Face array is null");
                } else {
                    int surfaceWidth = mPreview.getWidth();
                    int surfaceHeight = mPreview.getHeight();
                    faceObj.normalizeCoordinates(surfaceWidth, surfaceHeight);
                    preview.removeView(drawView); // Remove the previously created view to avoid unnecessary stacking of
                    // Views.
                    drawView = new DrawView(this, faceArray, true);
                    Log.i("TAG",""+faceArray);
                    preview.addView(drawView);
                }
            }
        }
    }

    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mformat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dformat = new SimpleDateFormat("dd");
        String strDate = "" + mformat.format(calendar.getTime()) + " " + dformat.format(calendar.getTime());
        return strDate;
    }

    public String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hformat = new SimpleDateFormat("h");
        SimpleDateFormat tformat = new SimpleDateFormat("mm");
        SimpleDateFormat apformat = new SimpleDateFormat("a");
        String strTime = "" + hformat.format(calendar.getTime()) + ":" + tformat.format(calendar.getTime()) + " " + apformat.format(calendar.getTime());
        return strTime;

    }
}
