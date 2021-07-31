package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int MAX_TWEET_LENGTH = 280;
    public static final String TAG = "ComposeActivity"; // defined TAG so we can log, convention is to name it the activity name

    EditText etCompose;
    Button btnTweet;
    TextView tvCharacterCount;
    Boolean formEnabled;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this); //Reference to Twitter client

        etCompose = findViewById(R.id.etCompose); //to retrieve text to be posted on twitter
        btnTweet = findViewById(R.id.btnTweet);   //onclick --> tweet (show up on timeline)
        tvCharacterCount = findViewById(R.id.tvCharacterCount);


        //Set a click listener on the button so we can access it and post it on timeline
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Your tweet cannot be empty!", Toast.LENGTH_LONG).show();
                    return; // so we don't make an API call to Twitter
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Please stay within the 140 character limit!", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                //Make an API call to Twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "published tweet: " + tweet);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            //set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            //closes the activity, pass data to parent
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }

                });

            }
        });

            etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharacterCount.setText(Integer.toString( MAX_TWEET_LENGTH - etCompose.length()));
                tvCharacterCount.setTextColor(etCompose.length() > MAX_TWEET_LENGTH ?
                        ResourcesCompat.getColor(getResources(), R.color.twitter_red, null) :
                        ResourcesCompat.getColor(getResources(), R.color.twitter_gray, null));
                setTweetButtonEnabled(etCompose.length() > 0 && etCompose.length() <= MAX_TWEET_LENGTH);
            }
        });

        setTweetButtonEnabled(false);
    }

      private void setTweetButtonEnabled(boolean enabled) {
        formEnabled = enabled;
        btnTweet.setEnabled(enabled);
        btnTweet.setAlpha((float) (enabled ? 1 : 0.6));
    }

    public void onCancelAction(View view) {
        finish();
    }

    }


