package com.bbm.oklar;

import android.os.Handler;
import android.os.Looper;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.appopen.AppOpenAd;

@CapacitorPlugin(name = "AppOpen")
public class AppOpenPlugin extends Plugin {
  private void err(PluginCall c,String s){JSObject o=new JSObject();o.put("hata",s);c.resolve(o);}
  @PluginMethod
  public void show(PluginCall call) {
    String id = call.getString("adId");
    final boolean[] done = {false};
    getActivity().runOnUiThread(() -> {
      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        if (!done[0]) { done[0] = true; err(call,"zaman asimi 4sn"); }
      }, 4000);
      AppOpenAd.load(getContext(), id, new AdRequest.Builder().build(),
        new AppOpenAd.AppOpenAdLoadCallback() {
          @Override public void onAdLoaded(AppOpenAd ad) {
            if (done[0]) return;
            done[0] = true;
            ad.setFullScreenContentCallback(new FullScreenContentCallback() {
              @Override public void onAdDismissedFullScreenContent() { call.resolve(); }
              @Override public void onAdFailedToShowFullScreenContent(AdError e) { err(call,"gosterim "+e.getCode()+": "+e.getMessage()); }
            });
            ad.show(getActivity());
          }
          @Override public void onAdFailedToLoad(LoadAdError e) {
            if (!done[0]) { done[0] = true; err(call,"kod "+e.getCode()+": "+e.getMessage()); }
          }
        });
    });
  }
}
