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

-keep class com.shockwave.**


# Restore some Source file names and restore approximate line numbers in the stack traces,
# otherwise the stack traces are pretty useless
-keepattributes SourceFile,LineNumberTable

# ACRA loads Plugins using reflection
-keep class * implements org.acra.plugins.Plugin {*;}

# ACRA uses enum fields in json
-keep enum org.acra.** {*;}

# autodsl accesses constructors using reflection
-keepclassmembers class * implements org.acra.config.Configuration { <init>(...); }

# ACRA creates a proxy for this interface
-keep interface org.acra.ErrorReporter

-dontwarn android.support.**

-dontwarn com.faendir.kotlin.autodsl.DslInspect
-dontwarn com.faendir.kotlin.autodsl.DslMandatory
-dontwarn com.google.auto.service.AutoService