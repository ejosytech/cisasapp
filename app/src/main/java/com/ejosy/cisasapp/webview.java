package com.ejosy.cisasapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class webview extends AppCompatActivity implements View.OnClickListener {

    WebView simpleWebView;
    Button loadWebPage, loadFromStaticHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // initiate buttons and a web view
        loadFromStaticHtml = (Button) findViewById(R.id.btn_main_home);
        loadFromStaticHtml.setOnClickListener(this);
        loadWebPage = (Button) findViewById(R.id.loadWebPage);
        loadWebPage.setOnClickListener(this);
        simpleWebView = (WebView) findViewById(R.id.simpleWebView);

        //
        Intent intent = getIntent();
        //
        String latv = intent.getStringExtra("lat_read");
        String longv = intent.getStringExtra("long_read");//if it's a string you stored.
        //
        StringBuilder str_latv = new StringBuilder(latv);
        StringBuilder str_longv = new StringBuilder(longv);
        // insert character value at offset 8
        str_latv.insert(1, '.');
        str_longv.insert(1, '.');
        //
        simpleWebView.setWebViewClient(new MyWebViewClient());
        String url = "http://maps.google.com/maps?q=loc:" + str_latv+ "," + str_longv;
        simpleWebView.getSettings().setJavaScriptEnabled(true);
        simpleWebView.loadUrl(url); // load a web page in a web view

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_home:
                // Revert Back Home
                Intent homeIntent =new Intent(getApplicationContext(),MainActivity.class);
                startActivity(homeIntent);

                break;
            case R.id.loadWebPage:

                Intent intent = getIntent();
                //
                String latv = intent.getStringExtra("lat_read");
                String longv = intent.getStringExtra("long_read");//if it's a string you stored.
                //
                //
                StringBuilder str_latv = new StringBuilder(latv);
                StringBuilder str_longv = new StringBuilder(longv);
                // insert character value at offset 8
                str_latv.insert(1, '.');
                str_longv.insert(1, '.');
                //
                simpleWebView.setWebViewClient(new MyWebViewClient());
                String url = "http://maps.google.com/maps?q=loc:" + str_latv + "," + str_longv;
                simpleWebView.getSettings().setJavaScriptEnabled(true);
                simpleWebView.loadUrl(url); // load a web page in a web view
                break;
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }


}
