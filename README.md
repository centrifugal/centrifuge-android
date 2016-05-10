# centrifuge-android
Centrifugo android client

### Usage
</br>
add
```
maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
```    
to <b>repositories</b>
and 
```
compile 'com.github.centrifugal:centrifuge-android:0.36-SNAPSHOT'
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
    compile 'com.github.centrifugal:centrifuge-android:0.36-SNAPSHOT'
}

```

Have a look at example [application](https://github.com/Centrifugal/centrifuge-android/tree/dev/app)
    
    
