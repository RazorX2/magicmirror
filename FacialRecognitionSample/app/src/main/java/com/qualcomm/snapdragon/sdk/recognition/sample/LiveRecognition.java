/*
 * =========================================================================
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * =========================================================================
 * @file LiveRecognition.java
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class LiveRecognition extends ListActivity implements Camera.PreviewCallback  {

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
    private FacialRecognitionActivity faceRecog;
    private HashMap<String, String> hash;
    public TextView Person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recognition);

        /***********************XML READING**************************/
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);

        CurrentWeatherForecast currentWeatherForecast = null;
        try {
            currentWeatherForecast = new CurrentWeatherForecast(); //pulls new forecast
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        CurrentWeatherCapsule weatherCapsule = currentWeatherForecast.getCurrentWeather();
        Log.d("XML Reading",weatherCapsule.temperature_string);
        FutureForecast futureForecast = null;
        try {
            futureForecast = new FutureForecast();//pulls current forecast
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Vector<FutureForecastCapsule> futureForecastCapsule = futureForecast.getFutureForecast();
        System.out.println();
        Log.d("XML Reading",futureForecastCapsule.toString());
        QuoteOfTheDay quoteOfTheDay = null;
        try {
            quoteOfTheDay = new QuoteOfTheDay(); //If new day new set of quotes
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        QuoteCapsule quoteCapsule = quoteOfTheDay.getQuote();//Randomized Quote Has Description and title
        Log.d("XML Reading",quoteCapsule.description);
        /*********************XML READING***************************/
        /*********************Date and Time*************************/
        final TextView Date = (TextView) findViewById(R.id.Date);
        final TextView Time = (TextView) findViewById(R.id.Time);
        Person = (TextView) findViewById(R.id.PersonView);

        Date.setText(getCurrentDate());
        Time.setText(getCurrentTime());
        /*********************Date and Time*************************/
        faceObj = FacialRecognitionActivity.faceObj;
        vibrate = (Vibrator) LiveRecognition.this
                .getSystemService(Context.VIBRATOR_SERVICE);

        orientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {

            }
        };
        faceRecog = new FacialRecognitionActivity();
        hash = faceRecog.retrieveHash(this);

        /***************Threading********************************/
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
                                //Person.setText(whoseMans());


                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
        /**********Threading***********************************/
        /**********Twitter************************************/
        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("CNN")
                .build();
        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(userTimeline)
                .build();
        setListAdapter(adapter);
        /**********Twitter***********************************/

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
            cameraObj.release();
        }
        cameraObj = null;
    }

    /*
     * Method that handles initialization and starting of camera.
     */
    private void startCamera() {
        cameraObj = Camera.open(FRONT_CAMERA_INDEX); // Open the Front camera

        mPreview = new CameraSurfacePreview(LiveRecognition.this, cameraObj,
                orientationListener); // Create a new surface on which Camera will be displayed.
        preview = (FrameLayout) findViewById(R.id.cameraPreview2);
        preview.addView(mPreview);
        cameraObj.setPreviewCallback(LiveRecognition.this);
        frameWidth = cameraObj.getParameters().getPreviewSize().width;
        frameHeight = cameraObj.getParameters().getPreviewSize().height;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("TAG","Made it to recognition part");
        boolean result = false;
        faceObj.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        result = faceObj.setFrame(data, frameWidth, frameHeight, true,
                    rotationAngle);
        if (result) {
            int numFaces = faceObj.getNumFaces();
            if (numFaces == 0) {
                Log.d("Facial Detection", "No Face Detected");
                if (drawView != null) {
                    preview.removeView(drawView);
                    drawView = new DrawView(this, null, false);
                    preview.addView(drawView);
                    Person.setText("");
                }
            } else {
                faceArray = faceObj.getFaceData();
                if (faceArray == null) {
                    Log.d("Facial Detection", "Face array is null");
                } else {
                    int surfaceWidth = mPreview.getWidth();
                    int surfaceHeight = mPreview.getHeight();
                    faceObj.normalizeCoordinates(surfaceWidth, surfaceHeight);
                    preview.removeView(drawView); // Remove the previously created view to avoid unnecessary stacking of
                    // Views.
                    drawView = new DrawView(this, faceArray, true);



                    String selectedPersonId = Integer.toString(faceArray[0].getPersonId());

                    String personName = null;
                    Iterator<HashMap.Entry<String, String>> iter = hash.entrySet()
                            .iterator();
                    while (iter.hasNext()) {
                        HashMap.Entry<String, String> entry = iter.next();
                        if (entry.getValue().equals(selectedPersonId))
                            personName = entry.getKey();
                    }


                    Log.d("Facial Detection",""+personName);
                    if(personName !=null)
                        Person.setText("Hello "+personName);
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
    class CurrentWeatherForecast {

        private CurrentWeatherCapsule objBean;
        private final URL url;
        public CurrentWeatherForecast() throws MalformedURLException {
            url = new URL("http://w1.weather.gov/xml/current_obs/KIAD.xml");
            try {

                URLConnection con = url.openConnection();


                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                String fullStr = "";
                while ((inputLine = reader.readLine()) != null)
                    fullStr = fullStr.concat(inputLine + "\n");

                InputStream istream = url.openStream();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(istream);
                doc.getDocumentElement().normalize();


                NodeList nList = doc.getElementsByTagName("current_observation");
                Node nNode = nList.item(0);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    objBean = new CurrentWeatherCapsule();
                    objBean.location = getTagValue("location", eElement);
                    objBean.temperature_string = getTagValue("temperature_string", eElement);
                    objBean.heat_index_string = getTagValue("heat_index_string", eElement);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getTagValue(String sTag, Element eElement) {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                    .getChildNodes();

            Node nValue = (Node) nlList.item(0);

            return nValue.getNodeValue();

        }

        public CurrentWeatherCapsule getCurrentWeather()
        {
            return objBean;
        }

    }

    class CurrentWeatherCapsule {

        public String location;
        public String temperature_string;
        public String heat_index_string;

    }

    class FutureForecast {
        Vector<FutureForecastCapsule> vectParse;
        private FutureForecastCapsule objBean;
        private final URL url;


        public FutureForecast() throws MalformedURLException {
            url = new URL("http://www.myweather2.com/developer/weather.ashx?uac=3wGk2/np6s&uref=64441752-32ca-454a-a9bb-f28a08cd2a1b");
            try {
                vectParse = new Vector<FutureForecastCapsule>();
                URLConnection con = url.openConnection();


                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                String fullStr = "";
                while ((inputLine = reader.readLine()) != null)
                    fullStr = fullStr.concat(inputLine + "\n");

                InputStream istream = url.openStream();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(istream);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("forecast");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        objBean = new FutureForecastCapsule();
                        vectParse.add(objBean);
                        //ADD TAGS
                        objBean.date = getTagValue("date", eElement);
                        objBean.day_max_temp = CToF(getTagValue("day_max_temp", eElement));
                        objBean.night_min_temp = CToF(getTagValue("night_min_temp", eElement));

                        NodeList nListTemp = doc.getElementsByTagName("day");
                        eElement = (Element)(nListTemp.item(temp));
                        objBean.day_weather_text = getTagValue("weather_text", eElement);

                        nListTemp = doc.getElementsByTagName("night");
                        eElement = (Element)(nListTemp.item(temp));
                        objBean.night_weather_text = getTagValue("weather_text", eElement);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String CToF(String s)
        {
            return ((9.0/5.0) * Integer.parseInt(s) + 32) + "";
        }

        private String getTagValue(String sTag, Element eElement) {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                    .getChildNodes();

            Node nValue = (Node) nlList.item(0);

            return nValue.getNodeValue();

        }

        public Vector getFutureForecast()
        {
            return vectParse;
        }

    }

    class FutureForecastCapsule {

        public String date;
        public String day_max_temp;
        public String night_min_temp;
        public String day_weather_text;
        public String night_weather_text;

    }

    class QuoteOfTheDay {
        Vector<QuoteCapsule> vectParse;
        private QuoteCapsule objBean;
        private final URL url;


        public QuoteOfTheDay() throws MalformedURLException {
            url = new URL("http://feeds.feedburner.com/quotationspage/qotd");
            try {
                vectParse = new Vector<QuoteCapsule>();
                URLConnection con = url.openConnection();


                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                String fullStr = "";
                while ((inputLine = reader.readLine()) != null)
                    fullStr = fullStr.concat(inputLine + "\n");

                InputStream istream = url.openStream();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(istream);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("item");

                for (int temp = 2; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        objBean = new QuoteCapsule();
                        vectParse.add(objBean);
                        //ADD TAGS
                        objBean.title = getTagValue("title", eElement);
                        objBean.description = getTagValue("description", eElement);

                    }
                }
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private String getTagValue(String sTag, Element eElement) {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                    .getChildNodes();

            Node nValue = (Node) nlList.item(0);

            return nValue.getNodeValue();

        }

        public QuoteCapsule getQuote()
        {
            return vectParse.get((int)(Math.random() * 9));
        }

    }

    class QuoteCapsule {
        public String title;
        public String description;

    }
}
