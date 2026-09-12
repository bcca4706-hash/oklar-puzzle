package com.bbm.oklar;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;

@CapacitorPlugin(name = "YandexAds")
public class YandexAds extends Plugin {

    private static final String AD_UNIT_ID = "R-M-20031432-1";

    private InterstitialAdLoader loader;
    private InterstitialAd ad;
    private boolean initialized = false;

    @PluginMethod
    public void init(PluginCall call) {
        Activity act = getActivity();
        act.runOnUiThread(() -> MobileAds.initialize(act, () -> {
            initialized = true;
            loader = new InterstitialAdLoader(act);
            loader.setAdLoadListener(new InterstitialAdLoadListener() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd loaded) {
                    ad = loaded;
                }
                @Override
                public void onAdFailedToLoad(@NonNull AdRequestError e) {
                    ad = null;
                }
            });
            loadAd();
        }));
        call.resolve();
    }

    private void loadAd() {
        if (loader == null) return;
        loader.loadAd(new AdRequestConfiguration.Builder(AD_UNIT_ID).build());
    }

    @PluginMethod
    public void show(PluginCall call) {
        Activity act = getActivity();
        act.runOnUiThread(() -> {
            JSObject r = new JSObject();
            if (ad == null) {
                if (initialized) loadAd();
                r.put("shown", false);
                call.resolve(r);
                return;
            }
            ad.setAdEventListener(new InterstitialAdEventListener() {
                @Override public void onAdShown() {}
                @Override public void onAdFailedToShow(@NonNull AdError e) { ad = null; loadAd(); }
                @Override public void onAdDismissed() { ad = null; loadAd(); }
                @Override public void onAdClicked() {}
                @Override public void onAdImpression(ImpressionData d) {}
            });
            ad.show(act);
            r.put("shown", true);
            call.resolve(r);
        });
    }
}
