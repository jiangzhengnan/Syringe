简体中文 | [English](./README.en.md) |<br />
# Syringe
![Java](https://img.shields.io/badge/language-Java-red.svg)
![visitors](https://visitor-badge.laobi.icu/badge?page_id=jiangzhengnan.syringe.read.me)
</br>
📌  插件化注入工程</br>

## 介绍
主流的热加载&修复框架类型</br>
● ClassLoader：将热修复的类放在dexElements[]的最前面，这样加载类时会优先加载到要修复的类以达到修复目的。如腾讯的Tinker、Nuwa等。</br>
● Native hook：底层替换，修改java方法在native层的函数指针，指向修复后的方法以达到修复目的。如阿里的Andifix、DexPosed等。</br>
● Instant run：在编译打包阶段对每个函数都插入一段控制逻辑代码。如美团的Robust。</br>
● 代理四大组件，如dynamic-load-apk</br>
● AAB方式，如Qigsaw</br>
</br>
本框架希望侧重于减少包体积size，不断集成并持续更新目前各种热加载以及热修复手段，以期在我们具体的使用时，在不同场景下达到灵活运用和最佳实践。</br>
持续开发中。</br>

### 引入方式
待上传到Maven,目前可以clone项目然后主动依赖
<br/>

### 使用方式
1.在Application中初始化Syringe</br>
```
public class MyApplication extends MultiDexApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Syringe.init(this);
    }
}
```
2.代理(占坑)方式启动热加载的Activity</br>
```
Intent i = new Intent(this, ProxyStubActivity.class);
i.putExtra(ProxyStubActivity.INTENT_CLASS_NAME, "com.ng.game.NgGameLevelOneActivity");
i.putExtra(ProxyStubActivity.INTENT_RES_PATH, "/storage/emulated/0/AAAAA/game-debug.apk");
tartActivity(i);
```
3.hook方式启动热加载的Activity</br>
```
Intent i = new Intent(this, HookStubActivity.class);
i.putExtra("targetActivity", "com.ng.novel.NgNovelActivity");
startActivity(i);
```
4.
具体实现参考Demo：
https://github.com/jiangzhengnan/Syringe/blob/master/app/src/main/java/com/ng/demo/test/MainActivity.java</br>

## 待完成需求
- [x] Service和ContentProvider的热加载实现
- [x] aab方式实现热加载
- [x] 补丁包版本控制
- [x] 补丁包差量分析，自定合成
- [x] 打包脚本自动化
### License

    Copyright 2021, Jiang Zhengnan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
