package com.ng.syringebuild

import com.android.build.gradle.AppExtension
import com.ng.syringebuild.transform.ResourcesLoaderTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class SyringeAppBasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("======SyringeAppBasePlugin apply 插件开始注册   ======")
        registerForApp(project)
    }

    /**
     * 注册 for android 工程
     */
    static void registerForApp(Project project) {
        AppExtension appExtension = project.extensions.getByType(AppExtension.class)

        //获取入参
        //AnalyseHelper.getInstance().setTransformExtension(project.getExtensions().getByName(HookExtensions.XERATH_HOOK_TRAFFIC_INFO))

        appExtension.registerTransform(new ResourcesLoaderTransform(project))
    }

}