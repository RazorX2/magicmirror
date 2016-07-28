package com.example.intern2.testproject;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

class Product
{

    public String name;
    public String quantity;
    public String color;

}

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiate TextView
        TextView Log = (TextView) findViewById(R.id.Logger);
        Log.setMovementMethod(new ScrollingMovementMethod());
        XmlPullParserFactory pullParserFactory;
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "papyrus.TTF");
        Log.setTypeface(typeFace);


        try
        {
            XmlPullParser parser = getResources().getXml(R.xml.temp);
            Log.setText(parseXML(parser));

        } catch (XmlPullParserException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    {
        ArrayList<Product> products = null;
        int eventType = parser.getEventType();
        Product currentProduct = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    products = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("product")){
                        currentProduct = new Product();
                    } else if (currentProduct != null){
                        if (name.equals("productname")){
                            currentProduct.name = parser.nextText();
                        } else if (name.equals("productcolor")){
                            currentProduct.color = parser.nextText();
                        } else if (name.equals("productquantity")){
                            currentProduct.quantity= parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("product") && currentProduct != null){
                        products.add(currentProduct);
                    }
            }
            eventType = parser.next();
        }

       return printProducts(products);
    }

    private String printProducts(ArrayList<Product> products)
    {
        String content = "";
        Iterator<Product> iter = products.iterator();
        while(iter.hasNext()) {
            Product currProduct = iter.next();
            content = content + "Product :" + currProduct.name + "\n";
            content = content + "Quantity :" + currProduct.quantity + "\n";
            content = content + "Color :" + currProduct.color + "\n\n\n";
            ;
        }
        return content;
    }





}




