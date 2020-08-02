# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class **.R$* {
    public static <fields>;
    public static final int *;
}

#error : Note: the configuration refers to the unknown class 'com.google.vending.licensing.ILicensingService'
#solution : @link http://stackoverflow.com/a/14463528
-dontnote com.google.vending.licensing.ILicensingService
-dontnote **ILicensingService

-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

##############################
#            admob           #
##############################
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.ads.mediation.admob.AdMobAdapter { *; }
-keep class com.google.ads.mediation.AdUrlAdapter { *; }

##############################
#       remove log code      #
##############################
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** v(...);
}
-assumenosideeffects class android.util.Log {
    public static *** i(...);
    public static *** v(...);
}
-assumenosideeffects class android.util.Log {
    public static *** w(...);
    public static *** v(...);
}

##############################
#            Umeng           #
##############################
-keepclassmembers class * {
    public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##############################
#        Umeng Online        #
##############################
-keepclassmembers class * {
    public <init> (org.json.JSONObject);
}
-keep class com.umeng.onlineconfig.OnlineConfigAgent {
    public <fields>;
    public <methods>;
}
-keep class com.umeng.onlineconfig.OnlineConfigLog {
    public <fields>;
    public <methods>;
}
-keep interface com.umeng.onlineconfig.UmengOnlineConfigureListener {
    public <methods>;
}

##############################
#            bugly           #
##############################
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}


##############################
#          AndoridX          #
##############################
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

##############################
#         ButterKnife        #
##############################
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

##############################
#            Glide           #
##############################
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder


##############################
#            GSON            #
##############################
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** {
    *;
}
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
#end GSON

##############################
#           Parcel           #
##############################
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public <fields>;
    private <fields>;
}
#end Parcel

##############################
#          EventBus          #
##############################
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

##############################
#          GreenDao          #
##############################
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
    public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
    public static void createTable(org.greenrobot.greendao.database.Database, boolean);
}
-keep class **$Properties { *; }

# If you DO use SQLCipher:
-keep class org.greenrobot.greendao.database.SqlCipherEncryptedHelper { *; }

# If you do NOT use SQLCipher:
-dontwarn net.sqlcipher.database.**
# If you do NOT use RxJava:
-dontwarn rx.**

##############################
#    yanzhenjie:permission   #
##############################
-dontwarn com.yanzhenjie.permission.**

##############################
#            OkGo            #
##############################
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
#okio
-dontwarn okio.**
-keep class okio.**{*;}

##############################
#           Volley           #
##############################
-keep class com.android.volley.** {*;}
-keep class com.android.volley.toolbox.** {*;}
-keep class com.android.volley.Response$* { *; }
-keep class com.android.volley.Request$* { *; }
-keep class com.android.volley.RequestQueue$* { *; }
-keep class com.android.volley.toolbox.HurlStack$* { *; }
-keep class com.android.volley.toolbox.ImageLoader$* { *; }

##############################
#          AgentWeb          #
##############################
-keep class com.just.agentweb.** {
    *;
}
-dontwarn com.just.agentweb.**

##############################
#            QMUI            #
##############################
-keep class **_FragmentFinder { *; }
-keep class com.qmuiteam.qmui.arch.record.** { *; }
-keep class androidx.fragment.app.* { *; }

########################################
#        AVLoadingIndicatorView        #
########################################
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

##############################
#            uCrop           #
##############################
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }


##############################
#             bean           #
##############################
-keep class **Bean { *; }
-keep class **Bean$* { *; }

##############################
#        WebsiteConfig       #
##############################
-keep class com.ess.anime.wallpaper.website.parser.**