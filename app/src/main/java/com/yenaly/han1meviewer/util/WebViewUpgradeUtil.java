package com.yenaly.han1meviewer.util;

import static com.yenaly.yenaly_libs.utils.ContextUtil.getApplicationContext;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.norman.webviewup.lib.UpgradeCallback;
import com.norman.webviewup.lib.WebViewUpgrade;
import com.norman.webviewup.lib.source.UpgradeAssetSource;
import com.norman.webviewup.lib.source.UpgradeSource;
import com.yenaly.han1meviewer.R;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WebViewUpgradeUtil {

    private static final String TAG = "WebViewUpgradeUtil";
    private static final String UPGRADE_PACKAGE_KEY = "arm64-v8a";

    private static final Map<String, List<UpgradeInfo>> UPGRADE_PACKAGE_MAP = new HashMap<>();

    static {
        UPGRADE_PACKAGE_MAP.put(UPGRADE_PACKAGE_KEY, List.of(
                new UpgradeInfo("com.google.android.webview",
                        "146.0.7680.115",
                        "com.google.android.webview.mp3",
                        "内置")));
    }


    public void Upgrade(Context context) {
        Context appContext = getApplicationContext();
        if (isSamsungDevice()) {
            Toast.makeText(appContext, R.string.webview_upgrade_samsung_unsupported, Toast.LENGTH_LONG).show();
            return;
        }

        if (!isHuaweiDevice()) {
            Toast.makeText(appContext, R.string.webview_upgrade_other_brand_hint, Toast.LENGTH_LONG).show();
            return;
        }

        if (!supportsBundledWebView()) {
            Toast.makeText(appContext, R.string.webview_upgrade_other_brand_hint, Toast.LENGTH_LONG).show();
            return;
        }

        List<UpgradeInfo> upgradeInfoList = UPGRADE_PACKAGE_MAP.get(UPGRADE_PACKAGE_KEY);
        if (upgradeInfoList == null || upgradeInfoList.isEmpty()) return;

        UpgradeInfo upgradeInfo = upgradeInfoList.get(0);
        if (WebViewUpgrade.isProcessing() || WebViewUpgrade.isCompleted()) return;

        WebViewUpgrade.addUpgradeCallback(new UpgradeCallback() {
            private boolean hasShownProcessToast;

            @Override
            public void onUpgradeProcess(float percent) {
                if (!hasShownProcessToast) {
                    hasShownProcessToast = true;
                    Toast.makeText(appContext, R.string.webview_upgrade_processing, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUpgradeComplete() {
                Toast.makeText(appContext, R.string.webview_upgrade_complete, Toast.LENGTH_SHORT).show();
                WebViewUpgrade.removeUpgradeCallback(this);
            }

            @Override
            public void onUpgradeError(Throwable throwable) {
                Toast.makeText(appContext, R.string.webview_upgrade_restart_required, Toast.LENGTH_LONG).show();
                Log.e(TAG, "message:" + throwable.getMessage() + "\nstackTrace:" + Log.getStackTraceString(throwable));
                WebViewUpgrade.removeUpgradeCallback(this);
            }
        });

        UpgradeSource upgradeSource = getUpgradeSource(upgradeInfo, context);
        WebViewUpgrade.upgrade(upgradeSource);
    }

    private static boolean isHuaweiDevice() {
        return containsIgnoreCase(Build.MANUFACTURER, "huawei")
                || containsIgnoreCase(Build.BRAND, "huawei");
    }

    private static boolean isSamsungDevice() {
        return containsIgnoreCase(Build.MANUFACTURER, "samsung")
                || containsIgnoreCase(Build.BRAND, "samsung");
    }

    private static boolean supportsBundledWebView() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && Arrays.asList(Build.SUPPORTED_ABIS).contains("arm64-v8a");
    }

    private static boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static UpgradeSource getUpgradeSource(UpgradeInfo upgradeInfo, Context context) {
        UpgradeSource upgradeSource = null;

        upgradeSource = new UpgradeAssetSource(
                context,
                upgradeInfo.url,
                new File(context.getFilesDir(), upgradeInfo.packageName + "/" + upgradeInfo.versionName + ".apk")
        );
        return upgradeSource;
    }


    static class UpgradeInfo {
        public UpgradeInfo(String packageName, String versionName, String url, String extraInfo) {
            this.title = packageName + "\n" + versionName;
            this.extraInfo = !TextUtils.isEmpty(extraInfo) ? extraInfo : "";
            if (!extraInfo.isEmpty()) {
                this.title = this.title + "\n" + extraInfo;
            }
            this.url = url;
            this.packageName = packageName;
            this.versionName = versionName;
        }

        public UpgradeInfo(String packageName, String versionName, String url) {

            this(packageName, versionName, url, "");
        }

        String title;
        String url;
        String packageName;
        String versionName;
        String extraInfo;
    }


}
