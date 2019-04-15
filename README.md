# AndResM
Android gradle plugin for resource package id modification(0x7f -> 0xpp)

support android-gradle-plugin 2.0.0+

:cn:[中文版戳这里](./README_CN.md)

[CHANGE LOG](https://github.com/xyxyLiu/AndResM/blob/master/CHANGELOG.md)

### Usage
Import the following code

* in your **root project** build.gradle
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        ......
        classpath 'com.reginald:andresm:0.3.0'
    }
}
```

* in your module build.gradle
```groovy
apply plugin: 'com.reginald.andresm'

andresm {
    packageId = 0x61
}
```

### How it works
Replace the outputs of aapt process including:
* resources.ap_ : arsc file, compiled xml files
* generated source: R.java

### Reference
* [android-arscblamer](https://github.com/google/android-arscblamer)