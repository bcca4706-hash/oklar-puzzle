package com.bbm.oklar;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.yandex.mobile.ads.appopenad.AppOpenAd;
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.YandexAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BBM Yandex reklam koprusu (Yandex Mobile Ads SDK 8.x).
 * JS:  YandexAds.init()
 *      YandexAds.load({key, adId, format:'interstitial'|'rewarded'|'appopen'}) -> {loaded}
 *      YandexAds.isLoaded({key}) -> {loaded}
 *      YandexAds.show({key}) -> {shown, rewarded, error?}   (reklam kapaninca doner)
 *      YandexAds.showBanner({adId}) -> {height}   ; YandexAds.hideBanner()
 * Olaylar: bannerHeight {height}, bannerFailed {error}
 */
@CapacitorPlugin(name = "YandexAds")
public class YandexAdsPlugin extends Plugin {

    private final Map<String, Object> ads = new HashMap<>();
    private final Map<String, Object> loaders = new HashMap<>();
    private final Set<String> loading = new HashSet<>();
    private BannerAdView banner;
    private boolean inited = false;

    private static String err(Object e) {
        if (e instanceof AdRequestError) return ((AdRequestError) e).getCode() + ": " + ((AdRequestError) e).getDescription();
        if (e instanceof AdError) return ((AdError) e).getDescription();
        return String.valueOf(e);
    }

    @PluginMethod
    public void init(final PluginCall call) {
        if (inited) { call.resolve(); return; }
        YandexAds.initialize(getContext(), new InitializationListener() {
            @Override
            public void onInitializationCompleted() {
                inited = true;
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void isLoaded(PluginCall call) {
        JSObject r = new JSObject();
        r.put("loaded", ads.get(call.getString("key", "")) != null);
        call.resolve(r);
    }

    @PluginMethod
    public void load(final PluginCall call) {
        final String key = call.getString("key");
        final String adId = call.getString("adId");
        final String format = call.getString("format", "interstitial");
        if (key == null || adId == null || adId.isEmpty()) { call.reject("key/adId yok"); return; }
        if (ads.get(key) != null) { ok(call, true); return; }
        if (loading.contains(key)) { ok(call, false); return; }
        loading.add(key);
        getActivity().runOnUiThread(() -> {
            AdRequest req = new AdRequest.Builder(adId).build();
            if ("rewarded".equals(format)) {
                RewardedAdLoader l = new RewardedAdLoader(getContext());
                loaders.put(key, l);
                l.loadAd(req, new RewardedAdLoadListener() {
                    @Override public void onAdLoaded(@NonNull RewardedAd ad) { loaded(key, ad, call); }
                    @Override public void onAdFailedToLoad(@NonNull AdRequestError e) { failed(key, e, call); }
                });
            } else if ("appopen".equals(format)) {
                AppOpenAdLoader l = new AppOpenAdLoader(getContext());
                loaders.put(key, l);
                l.loadAd(req, new AppOpenAdLoadListener() {
                    @Override public void onAdLoaded(@NonNull AppOpenAd ad) { loaded(key, ad, call); }
                    @Override public void onAdFailedToLoad(@NonNull AdRequestError e) { failed(key, e, call); }
                });
            } else {
                InterstitialAdLoader l = new InterstitialAdLoader(getContext());
                loaders.put(key, l);
                l.loadAd(req, new InterstitialAdLoadListener() {
                    @Override public void onAdLoaded(@NonNull InterstitialAd ad) { loaded(key, ad, call); }
                    @Override public void onAdFailedToLoad(@NonNull AdRequestError e) { failed(key, e, call); }
                });
            }
        });
    }

    private void ok(PluginCall call, boolean loaded) {
        JSObject r = new JSObject();
        r.put("loaded", loaded);
        call.resolve(r);
    }

    private void loaded(String key, Object ad, PluginCall call) {
        loading.remove(key);
        ads.put(key, ad);
        ok(call, true);
    }

    private void failed(String key, AdRequestError e, PluginCall call) {
        loading.remove(key);
        call.reject(err(e));
    }

    @PluginMethod
    public void show(final PluginCall call) {
        final String key = call.getString("key", "");
        final Object ad = ads.remove(key);
        if (ad == null) {
            JSObject r = new JSObject();
            r.put("shown", false);
            r.put("rewarded", false);
            r.put("error", "yuklu degil");
            call.resolve(r);
            return;
        }
        final boolean[] done = {false};
        final boolean[] rewarded = {false};
        final boolean[] shown = {false};
        final Runnable finish = () -> {
            if (done[0]) return;
            done[0] = true;
            JSObject r = new JSObject();
            r.put("shown", shown[0]);
            r.put("rewarded", rewarded[0]);
            call.resolve(r);
        };
        getActivity().runOnUiThread(() -> {
            if (ad instanceof RewardedAd) {
                RewardedAd a = (RewardedAd) ad;
                a.setAdEventListener(new RewardedAdEventListener() {
                    @Override public void onAdShown() { shown[0] = true; }
                    @Override public void onAdFailedToShow(@NonNull AdError e) { finish.run(); }
                    @Override public void onAdDismissed() { finish.run(); }
                    @Override public void onAdClicked() {}
                    @Override public void onAdImpression(@Nullable ImpressionData d) {}
                    @Override public void onRewarded(@NonNull Reward reward) { rewarded[0] = true; }
                });
                a.show(getActivity());
            } else if (ad instanceof AppOpenAd) {
                AppOpenAd a = (AppOpenAd) ad;
                a.setAdEventListener(new AppOpenAdEventListener() {
                    @Override public void onAdShown() { shown[0] = true; }
                    @Override public void onAdFailedToShow(@NonNull AdError e) { finish.run(); }
                    @Override public void onAdDismissed() { finish.run(); }
                    @Override public void onAdClicked() {}
                    @Override public void onAdImpression(@Nullable ImpressionData d) {}
                });
                a.show(getActivity());
            } else {
                InterstitialAd a = (InterstitialAd) ad;
                a.setAdEventListener(new InterstitialAdEventListener() {
                    @Override public void onAdShown() { shown[0] = true; }
                    @Override public void onAdFailedToShow(@NonNull AdError e) { finish.run(); }
                    @Override public void onAdDismissed() { finish.run(); }
                    @Override public void onAdClicked() {}
                    @Override public void onAdImpression(@Nullable ImpressionData d) {}
                });
                a.show(getActivity());
            }
        });
    }

    // ---------------- BANNER (alt, sticky) ----------------
    @PluginMethod
    public void showBanner(final PluginCall call) {
        final String adId = call.getString("adId");
        if (adId == null || adId.isEmpty()) { call.reject("adId yok"); return; }
        getActivity().runOnUiThread(() -> {
            if (banner != null) { removeBannerView(); }
            final DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            final float density = dm.density;
            int widthDp = Math.round(dm.widthPixels / density);
            final BannerAdView b = new BannerAdView(getContext());
            banner = b;
            b.setAdSize(BannerAdSize.sticky(getContext(), widthDp));
            final boolean[] answered = {false};
            b.setBannerAdEventListener(new BannerAdEventListener() {
                @Override public void onAdLoaded() {
                    b.post(() -> {
                        JSObject r = new JSObject();
                        r.put("height", Math.round(b.getHeight() / density));
                        notifyListeners("bannerHeight", r);
                        if (!answered[0]) { answered[0] = true; call.resolve(r); }
                    });
                }
                @Override public void onAdFailedToLoad(@NonNull AdRequestError e) {
                    JSObject r = new JSObject();
                    r.put("error", err(e));
                    notifyListeners("bannerFailed", r);
                    if (!answered[0]) {
                        answered[0] = true;
                        removeBannerView();
                        call.reject(err(e));
                    }
                }
                @Override public void onAdClicked() {}
                @Override public void onImpression(@Nullable ImpressionData d) {}
            });
            ViewGroup root = getActivity().findViewById(android.R.id.content);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            WindowInsetsCompat wi = ViewCompat.getRootWindowInsets(root);
            if (wi != null) {
                Insets in = wi.getInsets(WindowInsetsCompat.Type.navigationBars());
                lp.bottomMargin = in.bottom;
            }
            root.addView(b, lp);
            b.loadAd(new AdRequest.Builder(adId).build());
        });
    }

    @PluginMethod
    public void hideBanner(final PluginCall call) {
        getActivity().runOnUiThread(() -> {
            removeBannerView();
            call.resolve();
        });
    }

    private void removeBannerView() {
        if (banner == null) return;
        try {
            ViewGroup p = (ViewGroup) banner.getParent();
            if (p != null) p.removeView(banner);
            banner.destroy();
        } catch (Exception ignored) {}
        banner = null;
    }

    @Override
    protected void handleOnDestroy() {
        removeBannerView();
        super.handleOnDestroy();
    }
}
