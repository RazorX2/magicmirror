package com.example.intern1.twitterapi;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ListActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "GVHEcxqqeT8N9AFzk5W8WsDRN";
    private static final String TWITTER_SECRET = "ubmtsp81wXLwsLOeAGSdZ9V28cQzdgr6LC8XbnhjCveeDElekz";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        final UserTimeline userTimeline = new UserTimeline.Builder()
                .screenName("veyengar")
                .build();
        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(userTimeline)
                .build();
        setListAdapter(adapter);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ListView listView = getListView();
                int listLen = adapter.getCount();
                Log.d("listLen",listLen+"");
                while(true) {
                    for (int i = 1; i < 20; i++) {
                        try {
                            Thread.sleep(3000);
                            listView.smoothScrollToPosition(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    listView.smoothScrollToPosition(1);

                }
            }
        },4000);

    }
}
