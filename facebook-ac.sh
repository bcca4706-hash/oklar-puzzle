#!/data/data/com.termux/files/usr/bin/bash
# Facebook ile girisi acar.  Kullanim:  bash facebook-ac.sh <FACEBOOK_APP_ID> <FACEBOOK_CLIENT_TOKEN>
set -e
ID="$1"; TOKEN="$2"
if [ -z "$ID" ] || [ -z "$TOKEN" ]; then echo "Kullanim: bash facebook-ac.sh APP_ID CLIENT_TOKEN"; exit 1; fi
cd "$(dirname "$0")"
S=android/app/src/main/res/values/strings.xml
M=android/app/src/main/AndroidManifest.xml
if ! grep -q facebook_app_id "$S"; then
  sed -i "s#</resources>#    <string name=\"facebook_app_id\">$ID</string>\n    <string name=\"fb_login_protocol_scheme\">fb$ID</string>\n    <string name=\"facebook_client_token\">$TOKEN</string>\n</resources>#" "$S"
fi
if ! grep -q com.facebook.sdk.ApplicationId "$M"; then
  sed -i "s#</application>#        <meta-data android:name=\"com.facebook.sdk.ApplicationId\" android:value=\"@string/facebook_app_id\"/>\n        <meta-data android:name=\"com.facebook.sdk.ClientToken\" android:value=\"@string/facebook_client_token\"/>\n        <activity android:name=\"com.facebook.FacebookActivity\" android:configChanges=\"keyboard|keyboardHidden|screenLayout|screenSize|orientation\" android:label=\"@string/app_name\" />\n        <activity android:name=\"com.facebook.CustomTabActivity\" android:exported=\"true\">\n            <intent-filter>\n                <action android:name=\"android.intent.action.VIEW\" />\n                <category android:name=\"android.intent.category.DEFAULT\" />\n                <category android:name=\"android.intent.category.BROWSABLE\" />\n                <data android:scheme=\"@string/fb_login_protocol_scheme\" />\n            </intent-filter>\n        </activity>\n    </application>#" "$M"
fi
sed -i 's/rgcfaIncludeFacebook = false/rgcfaIncludeFacebook = true/' android/variables.gradle
sed -i 's/const FB_GIRIS=false;/const FB_GIRIS=true;/' www/index.html
echo "Facebook girisi acildi. Simdi: git add -A && git commit -m 'facebook giris' && git push"
