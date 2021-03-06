package com.example.intern2.readxmlonline;


import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView Log = (TextView) findViewById(R.id.Logger);
        Log.setMovementMethod(new ScrollingMovementMethod());

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Moon Light.ttf");
        Log.setTypeface(typeFace);

        CurrentWeatherForecast currentWeatherForecast = null;
        try {
            currentWeatherForecast = new CurrentWeatherForecast();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        CurrentWeatherCapsule weatherCapsule = currentWeatherForecast.getCurrentWeather();

        FutureForecast futureForecast = null;
        try {
            futureForecast = new FutureForecast();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Vector<FutureForecastCapsule> futureForecastCapsule = futureForecast.getFutureForecast();
        System.out.println();

        QuoteOfTheDay quoteOfTheDay = null;
        try {
            quoteOfTheDay = new QuoteOfTheDay();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        QuoteCapsule quoteCapsule = quoteOfTheDay.getQuote();
        System.out.println();

        String content = "";
        content += "Current Weather:\n\n\n";
        content += "Location: " + weatherCapsule.location + "\nTemperature: " + weatherCapsule.temperature_string + "\nHeat Index: " + weatherCapsule.heat_index_string;
        content += "\n\n";
        content += "Weather Forecast:\n\n\n";

        for (int i = 0; i < futureForecastCapsule.size(); i++)
        {
            content += "Date: " + futureForecastCapsule.get(i).date + "\nMax Temp: " + futureForecastCapsule.get(i).day_max_temp + "\nMin Temp: " + futureForecastCapsule.get(i).night_min_temp;
            content += "\nDay Weather: " + futureForecastCapsule.get(i).day_weather_text + "\nNight Weather: " + futureForecastCapsule.get(i).night_weather_text + "\n\n";
        }

        content += quoteCapsule.description + "\n-" + quoteCapsule.title;

        Log.setText(content);
    }

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
