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

-keep class sound.recorder.widget.model.MenuConfig$* { *; }
-keepclassmembers class sound.recorder.widget.model.MenuConfig { *; }

-keep class sound.recorder.widget.model.Song$* { *; }
-keepclassmembers class sound.recorder.widget.model.Song { *; }

-keep class sound.recorder.widget.model.Video$* { *; }
-keepclassmembers class sound.recorder.widget.model.Video{ *; }

-keep class sound.recorder.widget.model.VideoWrapper$* { *; }
-keepclassmembers class sound.recorder.widget.model.VideoWrapper { *; }

-keep class sound.recorder.widget.model.MyEventBus$* { *; }
-keepclassmembers class sound.recorder.widget.model.MyEventBus { *; }


-keep class sound.recorder.widget.notes.Note { *; }
-keepclassmembers class sound.recorder.widget.notes.Note { *; }


