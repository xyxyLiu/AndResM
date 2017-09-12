# AndResM
Android gradle plugin for resource package id modification(0x7f -> 0xpp)


:cn:[中文版戳这里](./README_CN.md)

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
        classpath 'com.reginald:andresm:0.1.0'
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