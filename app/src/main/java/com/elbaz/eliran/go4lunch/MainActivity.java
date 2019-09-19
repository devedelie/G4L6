package com.elbaz.eliran.go4lunch;

import android.os.Bundle;

import com.elbaz.eliran.go4lunch.base.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_main; }

}
