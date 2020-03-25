package com.sollian.customlintrules.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {

    public static void setStaticObjectField(String className, String fieldName, Object value) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 直接调用对象静态方法, 而忽略修饰符(private, protected, default)
     *
     * @param className : 子类
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @param parameters : 父类中的方法参数
     *
     * @return 父类中方法的执行结果
     */
    public static Object invokeStaticMethod(String className, String methodName,
                                            Class<?>[] parameterTypes, Object... parameters) {
        try {
            Method method = getDeclaredMethod(classForName(className), methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, parameters);
        } catch (Exception e) {
            throw new RuntimeException(
                    "invokeStaticMethod exception, className = " + className + ", methodName = "
                            + methodName,
                    e);
        }
    }

    public static Object invokeStaticMethod(ClassLoader loader, String className, String methodName, Class<?>[] parameterTypes, Object... parameters) {
        try {
            Class clz = Class.forName(className, false, loader);
            if (clz != null){
                Method method = getDeclaredMethod(clz, methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(null, parameters);
            }
        } catch (Exception e) {
            throw new RuntimeException("invokeStaticMethod exception, className = " + className + ", methodName = " + methodName, e);
        }

        return null;
    }

    public static Class classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("getClass exception, className = " + className, e);
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     *
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Object object, String methodName,
                                           Class<?>... parameterTypes) {
        Class<?> clazz = object instanceof Class ? (Class) object : object.getClass();
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException(
                "getDeclaredMethod exception, object = " + object + ", methodName = " + methodName);
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     *
     * @return 父类中的属性对象
     */
    public static Field getDeclaredField(Object object, String fieldName) {
        Class<?> clazz = object.getClass();
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (Exception ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException(
                "getDeclaredField exception, object = " + object + ", fieldName = " + fieldName);
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @param value : 将要设置的值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = getDeclaredField(object, fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(
                    "setFieldValue exception, object = " + object + ", fieldName = " + fieldName,
                    e);
        }
    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     *
     * @return : 父类中的属性值
     */
    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = getDeclaredField(object, fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(object);
        } catch (Exception e) {
            throw new RuntimeException(
                    "getFieldValue exception, object = " + object + ", fieldName = " + fieldName,
                    e);
        }
    }

}
