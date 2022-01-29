package com.ng.syringebuild.asm.resources;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 负责在 热加载的Aty 中 插入 getResources方法
 */
public class ResourcesLoaderClassVisitor extends ClassVisitor implements Opcodes, ResourcesLoaderWeaver {

    //是否需要插入
    private boolean needInsert = true;

    //类名
    private String owner;

    private String superName;

    private boolean isInterface;

    public ResourcesLoaderClassVisitor(ClassVisitor visitor) {
        super(ASM5, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.cv.visit(version, access, name, signature, superName, interfaces);
        this.owner = name;
        this.superName = superName;
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("getResources".equals(name)) {
            needInsert = false;
            System.out.println("ResourcesLoaderClassVisitor 方法已存在，需要插入 :" + needInsert);
            return new ResourcesLoaderMethodAdapter(Opcodes.ASM5, cv.visitMethod(access, name, desc, signature, exceptions), superName);
            //return new ChangeOnCreateMethodVisitor(Opcodes.ASM5, cv.visitMethod(access, name, desc, signature, exceptions), superName);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (needInsert) {
            System.out.println("ResourcesLoaderClassVisitor 方法不存在，构造新的 :" + needInsert);
            insertGetResourcesMethod(cv);
        }
        super.visitEnd();
    }

    static class ChangeOnCreateMethodVisitor extends MethodVisitor {

        String superClassName;

        ChangeOnCreateMethodVisitor(int api, MethodVisitor mv, String superClassName) {
            super(api, mv);
            this.superClassName = superClassName;
        }

        @Override
        public void visitCode() {
            mv.visitMethodInsn(INVOKESTATIC, "com/ng/syringe/Syringe", "instance", "()Lcom/ng/syringe/Syringe;", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "getResources", "()Landroid/content/res/Resources;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/ng/syringe/Syringe", "injectResources", "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false);
            super.visitCode();
        }
    }

    public void insertGetResourcesMethod(ClassVisitor cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(1, label0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "com/ng/syringe/Syringe", "instance", "()Lcom/ng/syringe/Syringe;", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "getResources", "()Landroid/content/res/Resources;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/ng/syringe/Syringe", "injectResources", "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(2, label1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "getResources", "()Landroid/content/res/Resources;", false);
        methodVisitor.visitInsn(ARETURN);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLocalVariable("this", "Lcom/ng/novel/NgNovelActivity;", null, label0, label2, 0);
        methodVisitor.visitMaxs(3, 1);
        methodVisitor.visitEnd();

//        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null);
//
//        mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, "instance", "()Lcom/ng/syringe/Syringe;", false);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false);
//        mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, METHOD_WOVEN, "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false);
//
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false);
//
//        mv.visitInsn(ARETURN);
//        mv.visitMaxs(2, 1);
//        mv.visitEnd();
    }
}
