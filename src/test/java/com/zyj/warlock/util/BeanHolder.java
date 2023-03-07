package com.zyj.warlock.util;

public class BeanHolder {
    public static Object bean;

    public static void setBean(Object obj) {
        BeanHolder.bean = obj;
    }

    public static <T> T getBean() {
        return (T) BeanHolder.bean;
    }
}
