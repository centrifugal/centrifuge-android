# ACentrifugo
Centrifugo android client

###Usage
</br>
add
```
maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
```    
to <b>repositories</b>
and 
```
compile 'com.github.sammyvimes:acentrifugo:0.32-SNAPSHOT'
```
to <b>dependencies</b> in your <b>build.gradle</b>    

so your build.gradle looks something like this:
```
apply plugin: 'com.android.application'

repositories {
    mavenCentral()
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}


android {
    compileSdkVersion 23
    buildToolsVersion '23.0.2'

    defaultConfig {
        applicationId "com.example.myapp"
        minSdkVersion 14
        targetSdkVersion 23
        versionCode 1
        versionName "1"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    testCompile 'junit:junit:4.12'
    compile 'com.github.sammyvimes:acentrifugo:0.32-SNAPSHOT'
}

```

Next:
in your AndroidManifest add
```
  <permission
      android:name="${applicationId}.permission.CENTRIFUGO_PUSH"
      android:protectionLevel="signature"/>
  <uses-permission android:name="${applicationId}.permission.CENTRIFUGO_PUSH"/>
```
and create a <b>BroadcastReceiver</b>
```
  <receiver android:name=".PushReceiver"
            android:exported="false">
      <intent-filter>
          <action android:name="${applicationId}.action.CENTRIFUGO_PUSH"/>
      </intent-filter>
  </receiver>
```

When you are ready to start push-service (you have id of client, token and it's timestamp), just call 
```
PushService.start(....) 
//note that there are multiple implementations of that function, that take different parameters
```

Example application:
https://github.com/SammyVimes/VersionMonitorAndroid
    
    
