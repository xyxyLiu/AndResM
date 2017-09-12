# AndResM
实现资源packageId改写的Android gradle插件

### Usage
请添加如下代码

* 在你的**根项目** 中的build.gradle中
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

* 在你的module的build.gradle中(如app/build.gradle)
```groovy
apply plugin: 'com.reginald.andresm'

andresm {
    packageId = 0x61 // 这里填写你想要替换的id，有效范围0x04-0x7f
}
```

### 工作原理
替换所有的aapt编译流程的输出：
* resources.ap_ : arsc文件及其它所有编译过的二进制xml文件
* 生成的源文件: R.java

这里的流程类似与[Small](https://github.com/wequick/Small)的
[资源id修改流程](https://github.com/wequick/Small/wiki/Android-dynamic-load-resources#repack-android-asset-package)，在此不再赘述

### 参考
* [android-arscblamer](https://github.com/google/android-arscblamer) Google非官方项目，一款用于解析arsc文件的命令行工具
* [android资源编译流程(罗升阳)](http://blog.csdn.net/luoshengyang/article/details/8744683)