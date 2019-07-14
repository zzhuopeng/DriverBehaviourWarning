package com.cqupt.driverbehaviourwarning.utils;

import java.util.LinkedList;

/**
 * 通过继承LinkedList方式，实现有序且不重复的集合
 */
public class SetList<T> extends LinkedList<T> {
    @Override
    public boolean add(T o) {
        if (0 == size()) {
            return super.add(o);
        }
        for (T object : this) {
            if (object.equals(o)) {
                return false;
            }
        }
        return super.add(o);
    }
}
