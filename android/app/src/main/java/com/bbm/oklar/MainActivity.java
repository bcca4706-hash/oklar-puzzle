package com.bbm.oklar;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(YandexAds.class);
        registerPlugin(AppOpenPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
