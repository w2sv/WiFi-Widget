# Workaround for missing Glance ActionCallback constructor keep rules.
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
