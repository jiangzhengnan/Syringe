package com.ng.syringebuild.asm.resources;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ResourcesLoaderMethodAdapter extends MethodVisitor implements ResourcesLoaderWeaver, Opcodes {

    String superClassName;

    ResourcesLoaderMethodAdapter(int api, MethodVisitor mv, String superClassName) {
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