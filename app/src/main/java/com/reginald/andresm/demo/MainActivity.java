package com.reginald.andresm.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = (TextView) findViewById(R.id.title_text);
        int strId = getResources().getIdentifier("test_str","string", getPackageName());
        if (strId > 0) {
            mTitle.setText(strId);
        } else {
            mTitle.setText("Can not find id!");
            mTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }
}
