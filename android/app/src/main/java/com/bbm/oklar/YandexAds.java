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
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

@CapacitorPlugin(name = "YandexAds")
public class YandexAds extends Plugin {

    private static final String REWARD_UNIT_ID = "R-M-20031432-1";

    private RewardedAdLoader loader;
    private RewardedAd ad;
    private boolean initialized = false;
    private boolean rewardEarned = false;
    private PluginCall pending = null;
    private String lastError = "";

    @PluginMethod
    public void init(PluginCall call) {
        Activity act = getActivity();
        act.runOnUiThread(() -> MobileAds.initialize(act, () -> {
            initialized = true;
            loader = new RewardedAdLoader(act);
            loader.setAdLoadListener(new RewardedAdLoadListener() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd loaded) {
                    ad = loaded;
                    lastError = "";
                }
                @Override
                public void onAdFailedToLoad(@NonNull AdRequestError e) {
                    ad = null;
                    lastError = e.getCode() + ":" + e.getDescription();
                }
            });
            loadAd();
        }));
        call.resolve();
    }

    private void loadAd() {
        if (loader == null) return;
        loader.loadAd(new AdRequestConfiguration.Builder(REWARD_UNIT_ID).build());
    }

    private void finish(boolean shown, boolean rewarded) {
        if (pending == null) return;
        JSObject r = new JSObject();
        r.put("shown", shown);
        r.put("rewarded", rewarded);
        r.put("error", lastError);
        pending.resolve(r);
        pending = null;
    }

    @PluginMethod
    public void showRewarded(PluginCall call) {
        Activity act = getActivity();
        act.runOnUiThread(() -> {
            if (ad == null) {
                if (initialized) loadAd();
                pending = call;
                finish(false, false);
                return;
            }
            rewardEarned = false;
            pending = call;
            ad.setAdEventListener(new RewardedAdEventListener() {
                @Override public void onAdShown() {}
                @Override public void onAdFailedToShow(@NonNull AdError e) {
                    lastError = "show:" + e.getDescription();
                    ad = null; loadAd();
                    finish(false, false);
                }
                @Override public void onAdDismissed() {
                    ad = null; loadAd();
                    finish(true, rewardEarned);
                }
                @Override public void onAdClicked() {}
                @Override public void onAdImpression(ImpressionData d) {}
                @Override public void onRewarded(@NonNull Reward reward) { rewardEarned = true; }
            });
            ad.show(act);
        });
    }
}
