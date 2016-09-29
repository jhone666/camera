package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.test.utils.Utils;

public class ShowCropperedActivity extends Activity {
    private static final String TAG = "ShowCropperedActivity";
    ImageView imageView;
    int beginHeight, endHeight, beginWidht, endWidth;
    CropperImage cropperImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_croppered);
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageURI(getIntent().getData());
    }
}
