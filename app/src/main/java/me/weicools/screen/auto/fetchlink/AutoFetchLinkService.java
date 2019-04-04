package me.weicools.screen.auto.fetchlink;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @author Weicools Create on 2019.04.03
 * <p>
 * desc:
 */
public class AutoFetchLinkService extends AccessibilityService {
    public static final String TAG = "AutoFetchLinkService";

    public AutoFetchLinkService() {}

    @Override
    public void onInterrupt() { }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        String className = event.getClassName().toString();
        String packageName = event.getPackageName().toString();
        Log.e(TAG, "onAccessibilityEvent: type-" + type + ", packageName-" + packageName + ", className-" + className);

        AccessibilityNodeInfo rootNodeInfo = null;
        try {
            rootNodeInfo = getRootInActiveWindow();
        } catch (Exception ignore) {
        }
        if (rootNodeInfo == null) {
            Log.e(TAG, "onAccessibilityEvent: getRootInActiveWindow is null");
            EventBus.getDefault().post(new MessageEvent.FetchedLinkEvent(type, packageName, className, "rootNodeInfo is null"));
            return;
        }

        List<AccessibilityNodeInfo> nodeInfoList = null;
        if (PackageConstants.CHROME.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.CHROME + ":id/url_bar");
        } else if (PackageConstants.FIREFOX.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.FIREFOX + ":id/url_bar_title");
        } else if (PackageConstants.YANDEX.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.YANDEX + ":id/bro_omnibar_address_title_text");
        } else if (PackageConstants.SAMSUNG.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.SAMSUNG + ":id/location_bar_edit_text");
        } else if (PackageConstants.EDGE.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.EDGE + ":id/url_bar");
        } else if (PackageConstants.DUCKDUCKGO.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.DUCKDUCKGO + ":id/omnibarTextInput");
        } else if (PackageConstants.DOLPHIN.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.DOLPHIN + ":id/title");
        } else if (PackageConstants.OPERA.equals(packageName)) {
            nodeInfoList = rootNodeInfo.findAccessibilityNodeInfosByViewId(PackageConstants.OPERA + ":id/url_field");
        }

        String url = "";
        int nodeListSize = 0;
        if (nodeInfoList != null && nodeInfoList.size() > 0) {
            nodeListSize = nodeInfoList.size();
            for (AccessibilityNodeInfo info : nodeInfoList) {
                if (info.getText() == null) {
                    continue;
                }
                url = info.getText().toString();
            }
        } else {
            url = "None";
        }

        Log.e(TAG, "onAccessibilityEvent: Url: " + url + ", nodeListSize: " + nodeListSize);
        EventBus.getDefault().post(new MessageEvent.FetchedLinkEvent(type, packageName, className, url));
    }
}
