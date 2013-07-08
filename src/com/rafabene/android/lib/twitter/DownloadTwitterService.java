package com.rafabene.android.lib.twitter;

import static com.rafabene.android.lib.Constant.LOG;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Downloads a Twitter from a specified query
 * 
 * @author rafaelbenevides
 * 
 */
public class DownloadTwitterService extends Service implements Runnable {

    public static final String CONSUMER_KEY = "consumerKey";
    public static final String CONSUMER_SECRET = "consumerSecret";
    public static final String QUERY_STRING = "queryString";
    private static final long delayMillis = 1000 * 60;

    private Handler handler = new Handler();

    private String consumerKey;
    private String consumerSecret;
    private String query;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        consumerKey = intent.getStringExtra(CONSUMER_KEY);
        consumerSecret = intent.getStringExtra(CONSUMER_SECRET);
        query = intent.getStringExtra(QUERY_STRING);
        if (consumerKey == null || consumerSecret == null || query == null){
            throw new IllegalStateException("Can't start DownloadTwitterService because it miss startup paramters");
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        Log.i(LOG, "DownloadTwitterService started");
        handler.post(this);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void run() {
        Twitter twitter = new TwitterUtil(this, consumerKey, consumerSecret).getTwitter();
        TwitterRepository twitterRepository = new TwitterRepository(this);

        Query q = new Query(query);
        try {
            Log.i(LOG, "Executing Twitter search: " + query);
            QueryResult results = twitter.search(q);
            for (Status status : results.getTweets()) {
                if (!twitterRepository.isTwitterPersisted(status.getId())) {
                    TwitterStatus twitterStatus = new TwitterStatus();
                    twitterStatus.setCreated(status.getCreatedAt());
                    twitterStatus.setFromUser(status.getUser().getScreenName());
                    twitterStatus.setId(status.getId());
                    twitterStatus.setProfileImageUrl(status.getUser().getProfileImageURL());
                    twitterStatus.setText(status.getText());
                    twitterRepository.insert(twitterStatus);
                    Log.i(LOG, "Inserting new twitter id: " + twitterStatus.getId());
                }
            }
        } catch (TwitterException e) {
            Log.e(LOG, "Error downloading twitter: " + e.getMessage(), e);
        }finally{
            handler.postDelayed(this, delayMillis);
        }
    }

}
