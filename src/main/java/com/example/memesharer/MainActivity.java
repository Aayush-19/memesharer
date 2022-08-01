package com.example.memesharer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONObject;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    Stack<String> prevMemes = new Stack<>();
    String imgUrl="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isConnected = checkConnectivity();

        if(isConnected) {
            loadMeme();

        }
    }

    public void loadMeme(){
        String url = "https://meme-api.herokuapp.com/gimme";
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            imgUrl = response.getString("url");
                            ImageView memeImg = findViewById(R.id.memeImg);
                            ProgressBar loading = findViewById(R.id.loadingBar);
                            loading.setVisibility(View.VISIBLE);
                            Glide.with( MainActivity.this).load(imgUrl).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    // log exception

                                    Log.e("TAG", "Error loading image", e);
                                    return false; // important to return false so the error placeholder can be placed
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    loading.setVisibility(View.GONE);
                                    return false;
                                }
                            }).into(memeImg);
                            prevMemes.add(imgUrl);
                            Log.d("success",  "url");
                        }
                        catch (Exception e){
                            Log.d("error",  "Something went wrong");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("error",  "Something went wrong");
             }
        });

// Add the request to the RequestQueue.
        SingleTonClass.getInstance(MainActivity.this).addToRequestQueue(jsonRequest);
    }

    public boolean checkConnectivity(){
        TextView msg = findViewById(R.id.errorText);

        try{
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = manager.getActiveNetworkInfo();
            if(netInfo.isConnected()) {
                msg.setText("");
                return true;
            }
        }
        catch(Exception E){
//            Toast.makeText(this,"Device is not connected to Internet",Toast.LENGTH_LONG).show();
            ImageView meme= findViewById(R.id.memeImg);
            meme.setImageResource(0);
            msg.setText("Device is not connected to Internet\nPlease connect to internet");
        }
        return false;
    }

    public void shareMemeFun(View view) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT,"Hey, checkout this meme, I found in reddit\n"+imgUrl);
        share.setType("text/plain");
        Intent chooser = Intent.createChooser(share,"Share with ..." );
        startActivity(chooser);
    }

    public void nextMemeFun(View view) {
        if(checkConnectivity())
            loadMeme();
    }

    public void prevMemeFun(View view) {

        if(prevMemes.size()>1){
            prevMemes.pop();
            ImageView memeImg = findViewById(R.id.memeImg);
            Glide.with(MainActivity.this).load(prevMemes.peek()).into(memeImg);
        }
        else{
            if(!prevMemes.empty())
                prevMemes.pop();
            ImageView memeImg= findViewById(R.id.memeImg);
            memeImg.setImageResource(0);
            TextView noMemeText = findViewById(R.id.errorText);
            noMemeText.setText("No previous meme available\n" + "Tap next to reload new meme");
        }
    }
}