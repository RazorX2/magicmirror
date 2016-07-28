package razorx2.magicmirror2;

import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Vibrator;
import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback{
    Camera cameraObj; // Accessing the Android native Camera.
    FrameLayout preview;
    private int FRONT_CAMERA_INDEX = 1;
    private int BACK_CAMERA_INDEX = 0;
    private OrientationEventListener orientationListener;
    private FacialProcessing faceObj;
    private int frameWidth;
    private int frameHeight;
    private boolean cameraFacingFront = true;
    private static FacialProcessing.PREVIEW_ROTATION_ANGLE rotationAngle = FacialProcessing.PREVIEW_ROTATION_ANGLE.ROT_90;
    private com.qualcomm.snapdragon.sdk.recognition.sample.DrawView drawView;
    private FaceData[] faceArray; // Array in which all the face data values will be returned for each face detected.
    private ImageView switchCameraButton;
    private Vibrator vibrate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final TextView Date = (TextView)findViewById(R.id.Date);
        final TextView Time = (TextView)findViewById(R.id.Time);
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
        faceObj = (FacialProcessing)FacialProcessing.getInstance();;
        t.start();


    }
    public String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mformat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dformat = new SimpleDateFormat("dd");
        String strDate = "" + mformat.format(calendar.getTime())+" "+ dformat.format(calendar.getTime());
        return strDate;
    }
    public String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hformat = new SimpleDateFormat("h");
        SimpleDateFormat tformat = new SimpleDateFormat("mm");
        SimpleDateFormat apformat = new SimpleDateFormat("a");
        String strTime = ""+hformat.format(calendar.getTime())  +   ":" +   tformat.format(calendar.getTime())  +   " " +   apformat.format(calendar.getTime());
        return strTime;

    }
    public void onPreviewFrame(byte[] data, Camera camera) {
        boolean result = false;
        faceObj.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        result = faceObj.setFrame(data, frameWidth, frameHeight, true,
                rotationAngle);
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
                    preview.addView(drawView);
                }
            }
        }
    }
}
//class myTask extends TimerTask{
//    MainActivity main = new MainActivity();
//    TextView Date;
//    TextView Time;
//    public myTask(MainActivity main, TextView date, TextView time){
//        main = main;
//        Date=date;
//        Time=time;
//    }
//    public void run(){
//        main.Update();
//    }
//}

