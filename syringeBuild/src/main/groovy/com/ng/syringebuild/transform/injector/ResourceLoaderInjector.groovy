package com.ng.syringebuild.transform.injector

import com.android.SdkConstants
import com.android.ide.common.internal.WaitableExecutor
import com.ng.syringebuild.asm.resources.ResourcesLoaderClassVisitor
import com.ng.syringebuild.transform.util.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.regex.Matcher

class ResourceLoaderInjector {

    WaitableExecutor waitAbleExecutor

    ResourceLoaderInjector(WaitableExecutor waitAbleExecutor) {
        this.waitAbleExecutor = waitAbleExecutor;
    }

    void injectDir(File outputDir) {
        Files.walk(outputDir.toPath(), Integer.MAX_VALUE).filter {
            Files.isRegularFile(it)
        }.each { Path path ->
            File file = path.toFile()
            if (file.name.endsWith(SdkConstants.DOT_JAR)) {
                injectJar(file)
            } else if (file.name.endsWith(SdkConstants.DOT_CLASS)) {
                this.waitAbleExecutor.execute {
                    String className = file.absolutePath.substring(outputDir.absolutePath.length() + 1, file.absolutePath.length() - SdkConstants.DOT_CLASS.length())
                            .replaceAll(Matcher.quoteReplacement(File.separator), '.')
                    byte[] bytes = injectClass(path, className)
                    if (bytes != null) {
                        Files.write(path, bytes, StandardOpenOption.WRITE)
                    }
                }
            }
        }
    }

    void injectJar(File jar) {
        this.waitAbleExecutor.execute {
            Map<String, String> zipProperties = ['create': 'false']
            URI zipDisk = URI.create("jar:${jar.toURI().toString()}")
            FileSystem zipFs = null
            try {
                zipFs = FileSystems.newFileSystem(zipDisk, zipProperties)
                Path root = zipFs.rootDirectories.iterator().next()
                Files.walk(root, Integer.MAX_VALUE).filter {
                    Files.isRegularFile(it)
                }.each { Path path ->
                    String pathString = path.toString().substring(1).replace("\\", "/")
                    if (!pathString.endsWith(SdkConstants.DOT_CLASS)) {
                        return
                    }
                    String className = pathString.replaceAll("/", '.').replace(SdkConstants.DOT_CLASS, "")
                    byte[] bytes = injectClass(path, className)
                    if (bytes != null) {
                        Files.write(path, bytes, StandardOpenOption.WRITE)
                    }
                }
            } catch (e) {
                e.printStackTrace()
            } finally {
                FileUtils.closeQuietly(zipFs)
            }
        }
    }

    byte[] injectClass(Path path, String className) {
        byte[] ret = null
        if (className.contains("NgNovelActivity")) {
            println "[ Syringe ] --- 开始注入 className: " + className + " " + path
            ret = weave(path.newInputStream())
            println "[ Syringe ] --- 完成注入 className: " + className + " " + path
        }
        return ret
    }


    byte[] weave(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor cv = new ResourcesLoaderClassVisitor(cw)
        cr.accept(cv, Opcodes.ASM5)
        return cw.toByteArray()
    }


}