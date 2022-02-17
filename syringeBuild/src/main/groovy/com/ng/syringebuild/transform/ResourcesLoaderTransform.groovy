package com.ng.syringebuild.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.ng.syringebuild.transform.injector.ResourceLoaderInjector
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
/**
 * 框架Transform
 */
class ResourcesLoaderTransform extends Transform {
    final static String NAME = "ResourcesLoaderTransform"

    private WaitableExecutor waitableExecutor

    private Project project

    ResourcesLoaderTransform(Project project) {
        this.project = project
        this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool();
        //project.getExtensions().create(HookExtensions.NOAH_HOOK_TRAFFIC_INFO, TransformExtension.class)
        println("======ResourcesLoaderTransform 插件初始化======")
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        long startTime = System.currentTimeMillis()
        ResourceLoaderInjector resourceLoaderInjector = new ResourceLoaderInjector(waitableExecutor)
        transformInvocation.getOutputProvider().deleteAll()
        //遍历输入
        for (TransformInput input in transformInvocation.inputs) {
            //当前app遍历
            for (DirectoryInput dirInput in input.directoryInputs) {
                File outputDir = transformInvocation.outputProvider.getContentLocation(dirInput.file.absolutePath, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, outputDir)
                println("======transform App ======" + outputDir.name)
                resourceLoaderInjector.injectDir(outputDir)
            }
            //jar遍历
            for (JarInput jarInput : input.jarInputs) {//jar（第三方库，module）
                File outputJar = transformInvocation.outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, outputJar)
                println("======transform Jar ======" + outputJar.name)
                resourceLoaderInjector.injectDir(outputJar)
            }
        }

        waitableExecutor.waitForTasksWithQuickFail(true)
        System.out.println(NAME + " cost " + (System.currentTimeMillis() - startTime) + " ms")
    }


}