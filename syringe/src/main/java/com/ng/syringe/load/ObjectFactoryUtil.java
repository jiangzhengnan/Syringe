package com.ng.syringe.load;


import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ObjectFactoryUtil {
    private final static HashMap<Class, String> mapMemberClass = new HashMap<Class, String>(8);

    static {
        mapMemberClass.put(Integer.class, "int");
        mapMemberClass.put(Double.class, "double");
        mapMemberClass.put(Float.class, "float");
        mapMemberClass.put(Character.class, "char");
        mapMemberClass.put(Boolean.class, "boolean");
        mapMemberClass.put(Short.class, "short");
        mapMemberClass.put(Long.class, "long");
        mapMemberClass.put(Byte.class, "byte");
    }

    public static <T> T invokeStaticMethod(Context context, String classname, String methodName, Object... pram) {
        return invokeMethod(context, null, classname, methodName, pram);
    }

    public static <T> T invokeMethod(Context context, Object object, String classname, String methodName,
                                     Object... pram) {
        if (classname == null || methodName == null || classname.length() == 0 || methodName.length() == 0) {
            return null;
        }
        try {
            Class<T> ap = (Class<T>) context.getClassLoader().loadClass(classname);
            Method[] methods = ap.getDeclaredMethods();
            Method method = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)
                        && isFound(methods[i].getParameterTypes(), pram)) {
                    method = methods[i];
                    break;
                }
            }
            if (method == null) {
                throw new IllegalArgumentException("[" + classname + "." + methodName + "]" + "No function found corresponding to the parameter type");
            }
            method.setAccessible(true);
            return (T) method.invoke(object, pram);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T make(Context context, Class<T> clazz, Object... pram) {
        return make(context, clazz.getName(), pram);
    }

    public static <T> T make(Context context, String classname, Object... pram) {
        if (classname == null || classname.length() == 0) {
            return null;
        }
        try {
            Class<T> ap = (Class<T>) context.getClassLoader().loadClass(classname);
            Constructor<T>[] constructors = (Constructor<T>[]) ap.getDeclaredConstructors();
            Constructor<T> constructor = null;
            for (int i = 0; i < constructors.length; i++) {
                Class[] aClass = constructors[i].getParameterTypes();
                //????????????????????????????????????
                if (isFound(aClass, pram)) {
                    constructor = constructors[i];
                    break;
                }
            }
            if (constructor == null) {
                throw new IllegalArgumentException("[" + classname + "]" + "not has parameter type constructor,if this is a innerClass,please see :https://github.com/miqt/WandFix/wiki/");
            }
            constructor.setAccessible(true);
            T o = constructor.newInstance(pram);
            return o;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ?????????????????????????????????Class[] ???????????????
     *
     * @param aClass
     * @param pram
     * @return
     */
    private static boolean isFound(Class[] aClass, Object[] pram) {
        if (aClass == null || pram == null) {
            return false;
        }
        if (aClass.length != pram.length) {
            return false;
        }
        for (int j = 0; j < aClass.length; j++) {
            List<String> baseClassList = getBaseClass(pram[j].getClass());
            if (!baseClassList.contains(aClass[j].getName())) {
                return false;
            }
        }
        return true;
    }

    private static List<String> getBaseClass(Class clazz) {
        List<String> result = new LinkedList<>();
        result.addAll(getSuperClass(clazz));
        result.addAll(getInterfaces(clazz));
        return result;
    }

    private static List<String> getInterfaces(Class clazz) {
        List<String> result = new LinkedList<>();
        if (clazz == null) {
            return result;
        }
        Class[] classes = clazz.getInterfaces();
        for (int i = 0; i < classes.length; i++) {
            result.add(classes[i].getName());
        }
        for (int i = 0; i < classes.length; i++) {
            result.addAll(getInterfaces(classes[i]));
        }
        return result;
    }

    private static List<String> getSuperClass(Class clazz) {
        List<String> result = new LinkedList<>();
        while (clazz != null) {
            String name = mapMemberClass.get(clazz);
            if (name != null) {
                result.add(name);
            }
            result.add(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return result;
    }
}
