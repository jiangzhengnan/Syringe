package com.ng.syringebuild.transform.injector

import com.ng.syringebuild.asm.base.ExtendClassWriter
import com.ng.syringebuild.asm.load.JarClassVisitor
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.security.SecureClassLoader
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Transform Jar
 */
class JarTransformExecutor {
    private static final FileTime ZERO = FileTime.fromMillis(0)

    //jar注入
    final static void weave(File inputJar, File outputJar, SecureClassLoader classLoader) throws IOException {
        ZipFile inputZip = new ZipFile(inputJar)
        ZipOutputStream outputZip = new ZipOutputStream(new BufferedOutputStream(
                Files.newOutputStream(outputJar.toPath())))
        Enumeration<? extends ZipEntry> inEntries = inputZip.entries()
        while (inEntries.hasMoreElements()) {
            ZipEntry entry = inEntries.nextElement()
            InputStream originalFile =
                    new BufferedInputStream(inputZip.getInputStream(entry))
            ZipEntry outEntry = new ZipEntry(entry.getName())
            byte[] newEntryContent
            if (!isWeaveClass(outEntry.getName().replace("/", "."))) {
                newEntryContent = IOUtils.toByteArray(originalFile)
            } else {
                newEntryContent = weaveSingleClassToByteArray(originalFile, classLoader)
            }
            CRC32 crc32 = new CRC32()
            crc32.update(newEntryContent)
            outEntry.setCrc(crc32.getValue())
            outEntry.setMethod(ZipEntry.STORED)
            outEntry.setSize(newEntryContent.length)
            outEntry.setCompressedSize(newEntryContent.length)
            outEntry.setLastAccessTime(ZERO)
            outEntry.setLastModifiedTime(ZERO)
            outEntry.setCreationTime(ZERO)
            outputZip.putNextEntry(outEntry)
            outputZip.write(newEntryContent)
            outputZip.closeEntry()
        }
        outputZip.flush()
        outputZip.close()
    }

    static boolean isWeaveClass(String fullQualifiedClassName) {
        if (fullQualifiedClassName == null) {
            return false
        }
        //过滤系统类
//        if (TransformUtil.isSystemClass(fullQualifiedClassName)) {
//            return false
//        }
//        //过滤noah类
//        if (PkgUtils.isNoah(fullQualifiedClassName)) {
//            return false
//        }
        //自定义需要抓取的类
//        if (PkgUtils. (fullQualifiedClassName)) {
//            println("")
//            println(fullQualifiedClassName + " 需要hook")
//            return true
//        }
//        if (RunModel.HOOK == AnalyseHelper.getInstance().getNoahTransformExtension().runModel) {
////            if ( isNeedHook(fullQualifiedClassName)) {
////                println("")
////                println(fullQualifiedClassName + " 需要hook")
////                return true
////            }
//        } else if (RunModel.ANALYSE == AnalyseHelper.getInstance().getNoahTransformExtension().runModel) {
//        }
        return false
    }

    //jar注入
    static byte[] weaveSingleClassToByteArray(InputStream inputStream, SecureClassLoader classLoader) throws IOException {
        ClassReader classReader = new ClassReader(inputStream)
        ClassWriter classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_FRAMES)
        //预处理ClassVisitor
        //NoahPreLoadClassVisitor preAdapter = new NoahPreLoadClassVisitor(classWriter)
        //classReader.accept(preAdapter, ClassReader.SKIP_FRAMES)
        //正式处理
        classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_FRAMES)
        ClassVisitor classWriterWrapper = new JarClassVisitor(classWriter)
        classReader.accept(classWriterWrapper, ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }

}