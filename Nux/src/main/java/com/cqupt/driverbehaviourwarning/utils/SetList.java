package com.cqupt.driverbehaviourwarning.utils;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 通过继承LinkedList方式，实现有序且不重复的集合
 */
public class SetList<T> extends LinkedBlockingQueue<T> {
    @Override
    public boolean offer(T o) {
        if (0 == size()) {
            return super.offer(o);
        }
        for (T object : this) {
            if (object.equals(o)) {
                return false;
            }
        }
        return super.offer(o);
    }
}
