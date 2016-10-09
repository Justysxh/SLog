package com.sxh.slogdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sxh.slog.SLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SLog.init(getApplicationContext());
        SLog.setDefaultTag("MyTag");
        SLog.setLogFile("/sxh","sxhlog.txt");
        SLog.i("test");
        SLog.i("tag2","this is tag2 msg");
        SLog.i("the current time mills: %d", System.currentTimeMillis());
        findViewById(R.id.btnTest).setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        SLog.i(""+(int)(Math.random()*10000));
    }
}
