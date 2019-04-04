package me.weicools.screen.auto.fetchlink;

/**
 * @author Weicools Create on 2019.04.03
 * <p>
 * desc:
 */
public class MessageEvent {
    public static class FetchedLinkEvent {
        public final int type;
        public final String packageName;
        public final String className;
        public final String url;

        public FetchedLinkEvent(int type, String packageName, String className, String url) {
            this.type = type;
            this.packageName = packageName;
            this.className = className;
            this.url = url;
        }
    }
}
