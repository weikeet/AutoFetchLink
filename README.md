## 通过无障碍服务自动获取浏览器访问的链接

### 可监听的事件类型列表
```java
TYPE_VIEW_CLICKED = 1   //View被点击
TYPE_VIEW_LONG_CLICKED = 2  //View被长按
TYPE_VIEW_SELECTED = 4  //View被选中
TYPE_VIEW_FOCUSED = 8   //View获得焦点
TYPE_VIEW_TEXT_CHANGED = 16 //View文本变化
TYPE_WINDOW_STATE_CHANGED = 32  //打开了一个PopWindow，Menu，Dialog
TYPE_NOTIFICATION_STATE_CHANGED = 64    //Notification发生变化
TYPE_VIEW_HOVER_ENTER = 128 //View进入悬停
TYPE_VIEW_HOVER_EXIT = 256  //View退出悬停
TYPE_TOUCH_EXPLORATION_GESTURE_START = 512  //触摸浏览事件开始
TYPE_TOUCH_EXPLORATION_GESTURE_END = 1024   //触摸浏览事件结束
TYPE_WINDOW_CONTENT_CHANGED = 2048//窗口内容发生变化，或子树跟布局发生变化
TYPE_VIEW_SCROLLED = 4096   //View滚动
TYPE_VIEW_TEXT_SELECTION_CHANGED = 8192 //EditText文字选中发生变化事件
TYPE_ANNOUNCEMENT = 16384   //应用产生一个通知事件
TYPE_VIEW_ACCESSIBILITY_FOCUSED = 32768 //获得无障碍事件焦点
TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED = 65536   //无障碍焦点清除
TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY = 131702
TYPE_GESTURE_DETECTION_START = 262144   //开始手势监测
TYPE_GESTURE_DETECTION_END = 524288 //结束手势监测
TYPE_TOUCH_INTERACTION_START = 1048576  //触摸屏幕事件开始
TYPE_TOUCH_INTERACTION_END = 2097152    //触摸屏幕事件结束
TYPE_WINDOWS_CHANGED = 4194304  //屏幕上的窗口变化事件，API>21
TYPE_VIEW_CONTEXT_CLICKED = 8388608 //View的上下文被点击
TYPE_ASSIST_READING_CONTEXT = 16777216  //辅助用户读取当前屏幕事件
```

### 获取控件ID
可以通过resource-id，text来定位到结点，如果可以，建议使用text来定位，因为一般APP更新后，这个id都可能会发生变化

#### UI Automator查看布局层次
>  实测LG V30(HavocOS Android P) 抓取一次之后，手机无法做任何操作(类似于)，必须重启手机才可以，有毒！

选中对应的控件，可以得到控件的相关信息
![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202019-04-03%20%E4%B8%8A%E5%8D%8810.42.15.png)



#### 开发者助手
如果你手机root了的话，可以安装一个**『开发者助手』**，点击当前界面分析，点击想查看的节点即可，如图所示。
![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-094134.jpg)

常见浏览器的地址栏控件ID如下：

| Chrome                                                       | Firefox                                                      | Yandex                                                       | Samsung                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-Screenshot__20190403-181637.png) | ![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-Screenshot__20190403-182351.png) | ![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-Screenshot__20190403-181751.png) | ![](https://blog-1251678165.cos.ap-chengdu.myqcloud.com/2019-04-03-Screenshot__20190403-181617.png) |



### 自动获取URL实现

#### 继承AccessibilityService

重新onAccessibilityEvent方法，通过getRootInActiveWindow()方法可得到根节点 AccessibilityNodeInfo，再通过包名和控件ID即可得到控件的信息。

有一点务必注意： resource-id不一定是唯一！另外，找结点要注意判空，找不到对应结点直接调用其他方法是会空指针异常的！

- getRootInActiveWindow()：获取当前整个活动窗口的根节点,返回的是一个AccessibilityNodeInfo类
- AccessibilityNodeInfo代表View的状态信息，提供了下述几个非常实用的方法：
  - findAccessibilityNodeInfosByViewId：通过视图id查找节点元素
  - findAccessibilityNodeInfosByText：通过字符串查找节点元素
  - getParent：获取父节点
  - getChild：获取子节点

```java
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
```

```java
public class PackageConstants {
    public static final String CHROME = "com.android.chrome";
    public static final String FIREFOX = "org.mozilla.firefox";
    public static final String YANDEX = "com.yandex.browser";
    public static final String SAMSUNG = "com.sec.android.app.sbrowser";
    public static final String EDGE = "com.microsoft.emmx";
    public static final String DUCKDUCKGO = "com.duckduckgo.mobile.android";
    public static final String DOLPHIN = "mobi.mgeek.TunnyBrowser";
    public static final String OPERA = "com.opera.browser";
}
```



#### 注册到manifest
```xml
<service
    android:name=".AutoFetchLinkService"
    android:enabled="true"
    android:exported="true"
    android:label="AutoFetchLinkService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>

    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibilityservice" />
</service>
```



#### accessibility service配置

/res/xml/accessibilityservice.xml

关键点：

accessibilityEventTypes：监听的事件类型，可同时监听多个事件

packageNames：监听的应用包名

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeViewFocused|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="true"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100"
   android:packageNames="com.android.chrome,org.mozilla.firefox,com.yandex.browser,com.sec.android.app.sbrowser" />
```