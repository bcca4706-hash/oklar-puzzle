package com.bbm.oklar;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

@CapacitorPlugin(name = "UnityAds")
public class UnityAdsPlugin extends Plugin {

    @PluginMethod
    public void init(final PluginCall call) {
        String gameId = call.getString("gameId");
        boolean testMode = Boolean.TRUE.equals(call.getBoolean("testMode", false));
        if (gameId == null) { call.reject("gameId gerekli"); return; }
        if (UnityAds.isInitialized()) { call.resolve(); return; }
        UnityAds.initialize(getContext().getApplicationContext(), gameId, testMode,
            new IUnityAdsInitializationListener() {
                @Override public void onInitializationComplete() { call.resolve(); }
                @Override public void onInitializationFailed(UnityAds.UnityAdsInitializationError e, String msg) {
                    call.reject("init: " + msg);
                }
            });
    }

    @PluginMethod
    public void load(final PluginCall call) {
        String pid = call.getString("placementId");
        if (pid == null) { call.reject("placementId gerekli"); return; }
        UnityAds.load(pid, new IUnityAdsLoadListener() {
            @Override public void onUnityAdsAdLoaded(String id) {
                JSObject r = new JSObject(); r.put("loaded", true); call.resolve(r);
            }
            @Override public void onUnityAdsFailedToLoad(String id, UnityAds.UnityAdsLoadError e, String msg) {
                call.reject("load: " + msg);
            }
        });
    }

    @PluginMethod
    public void show(final PluginCall call) {
        final String pid = call.getString("placementId");
        if (pid == null) { call.reject("placementId gerekli"); return; }
        getActivity().runOnUiThread(() ->
            UnityAds.show(getActivity(), pid, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                @Override public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError e, String msg) {
                    call.reject("show: " + msg);
                }
                @Override public void onUnityAdsShowStart(String id) {}
                @Override public void onUnityAdsShowClick(String id) {}
                @Override public void onUnityAdsShowComplete(String id, UnityAds.UnityAdsShowCompletionState state) {
                    JSObject r = new JSObject();
                    r.put("rewarded", state == UnityAds.UnityAdsShowCompletionState.COMPLETED);
                    call.resolve(r);
                }
            }));
    }
}
