package com.example.intern1.apitest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;



public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "GVHEcxqqeT8N9AFzk5W8WsDRN";
    private static final String TWITTER_SECRET = "	ubmtsp81wXLwsLOeAGSdZ9V28cQzdgr6LC8XbnhjCveeDElekz";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button_tweet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Twitter Button Clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),TimelineActivity.class);
                startActivity(intent);
            }
        });

    }
}
