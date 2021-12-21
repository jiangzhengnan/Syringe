# Syringe
📌  插件化注入工程

主流的热修复框架类型
● ClassLoader：将热修复的类放在dexElements[]的最前面，这样加载类时会优先加载到要修复的类以达到修复目的。如腾讯的Tinker、Nuwa等。
● Native hook：修改java方法在native层的函数指针，指向修复后的方法以达到修复目的。如阿里的Andifix、DexPosed等。
● Instant run：在编译打包阶段对每个函数都插入一段控制逻辑代码。如美团的Robust。
● 代理四大组件，如dynamic-load-apk
 
