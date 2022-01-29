package com.ng.syringebuild.asm.load;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;


/**
 * @author : jiangzhengnan
 * @creation : 2021/08/25
 * @description :
 * 负责分析
 */
public class JarCatchMethodVisitor extends MethodVisitor implements Opcodes {
    private int mMethodAccess;
    private boolean isStaticMethod;

    //参数
    private String mClassName;
    private String mMethodName;
    private String mMethodDesc;
    private String mMethodSignature;

    private List<Integer> varList = new ArrayList<>();

    protected JarCatchMethodVisitor(int api, int access, String className, String methodName, String descriptor,
                                    String signature, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
        mClassName = className;
        mMethodName = methodName;
        mMethodDesc = descriptor;
        mMethodSignature = signature;
        mMethodAccess = access;
        isStaticMethod = ((Opcodes.ACC_STATIC & access) != 0);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        varList = new ArrayList<>();
    }


}