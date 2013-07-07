package com.rafabene.android.lib.twitter;

import static android.content.Context.MODE_PRIVATE;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.widget.Toast;

import com.rafabene.android.lib.R;

public class TwitterUtil {

    private static final String REQUEST_TOKEN = "requestToken";
    private static final String REQUEST_TOKEN_SECRET = "requestTokenSecret";

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";

    private static final String TWITTER_PREF = "TwitterPreference";

    private Context context;

    private TwitterUtil(Context context) {
        this.context = context;
    }

    public static TwitterUtil getInstance(Context context) {
        return new TwitterUtil(context);
    }

    /**
     * Get Access Token using a previous RequestToken from {@link #askOAuth(String, String, String)} method
     * 
     * @param consumerKey
     * @param consumerSecret
     * @param verifier
     * @return
     * @throws TwitterException
     */
    public AccessToken getAccessTokenFromOAuthVerifier(String consumerKey, String consumerSecret, String verifier)
        throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        SharedPreferences sharedPreferences = context.getSharedPreferences(TWITTER_PREF, MODE_PRIVATE);
        String requestToken = sharedPreferences.getString(REQUEST_TOKEN, null);
        String requestTokenSecret = sharedPreferences.getString(REQUEST_TOKEN_SECRET, null);

        if (requestToken != null && requestTokenSecret != null) {
            RequestToken tr = new RequestToken(requestToken, requestTokenSecret);

            AccessToken accessToken = twitter.getOAuthAccessToken(tr, verifier);

            Editor editor = sharedPreferences.edit();
            //Store AccessToken
            editor.putString(ACCESS_TOKEN, accessToken.getToken());
            editor.putString(ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
            
            //Remove previous RequestToken
            editor.remove(REQUEST_TOKEN);
            editor.remove(REQUEST_TOKEN_SECRET);
            
            editor.commit();

            return accessToken;
        } else {
            throw new IllegalStateException(context.getString(R.string.twitter_illegalState));
        }
    }

    /**
     * Convenience method to load {@link AccessToken} from file system
     * 
     * @return
     */
    public AccessToken loadAccessToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TWITTER_PREF, MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(ACCESS_TOKEN, null);
        String accessTokenSecret = sharedPreferences.getString(ACCESS_TOKEN_SECRET, null);
        if (accessToken != null && accessTokenSecret != null) {
            return new AccessToken(accessToken, accessTokenSecret);
        } else {
            return null;
        }
    }

    /**
     * Convenience method to clear stored {@link AccessToken}
     */
    public void clearAccessToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TWITTER_PREF, MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.remove(ACCESS_TOKEN);
        editor.remove(ACCESS_TOKEN_SECRET);
        editor.commit();
    }

    /**
     * Open a browser to ask to authorize app to use Twitter.
     * 
     * The {@link RequestToken} is stored so it can be used again to complete the process on
     * {@link #getAccessTokenFromOAuthVerifier(String, String, String)} method
     * 
     * @param consumerKey
     * @param consumerSecret
     * @param callBackUrl
     * @throws TwitterException
     */
    public void askOAuth(String consumerKey, String consumerSecret, String callBackUrl) throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        RequestToken requestToken = twitter.getOAuthRequestToken(callBackUrl);

        SharedPreferences sharedPreferences = context.getSharedPreferences(TWITTER_PREF, MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(REQUEST_TOKEN, requestToken.getToken());
        editor.putString(REQUEST_TOKEN_SECRET, requestToken.getTokenSecret());
        editor.commit();

        Toast.makeText(context, R.string.twitter_authorize, Toast.LENGTH_LONG).show();
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
    }
}
