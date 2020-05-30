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
        String resName = "app_name";
        int resId = getResources().getIdentifier(resName,"string", getPackageName());
        if (resId > 0) {
            mTitle.setText(String.format("R.string.%s = %s\nmy resId is 0x%s",
                    resName, getString(resId), Integer.toHexString(resId)));
        } else {
            mTitle.setText("Can NOT find res " + resName);
            mTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }
}
