package com.cqupt.driverbehaviourwarning.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 内部静态类，单例模式（目前只创建了scheduledThreadPool线程池）
 * <p>
 * 循环执行的Runnable可以用返回的Future.cancel(false)取消
 */
public class ThreadPoolUtils {

    private static final String TAG = "ThreadPoolUtils";

    private ScheduledExecutorService mScheduledThreadPool;

    /**
     * 构造方法私有，不能被外界调用
     */
    private ThreadPoolUtils() {
    }

    public static ThreadPoolUtils getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 内部静态类：用于实例化scheduledThreadPool线程池。
     */
    private static class SingletonHolder {
        private static final ThreadPoolUtils instance = new ThreadPoolUtils();
    }

    /**
     * 获取scheduledThreadPool线程池
     *
     * @return ScheduledExecutorService
     */
    public ScheduledExecutorService getScheduledThreadPool() {
        if (null == mScheduledThreadPool) {
            mScheduledThreadPool = Executors.newScheduledThreadPool(8);
        }
        return mScheduledThreadPool;
    }

    //用法记录
//    //3秒后任务得到一次执行
//        executor.schedule(onceTask, 3000, TimeUnit.MILLISECONDS);
//    //3秒后任务得到一次执行,后面每隔2秒执行一次（上一次任务的结束时间 + 延迟时间 = 下一次任务的开始时间）
//        executor.scheduleWithFixedDelay(FixedTask, 3000, 2000, TimeUnit.MILLISECONDS);
//    //3秒后任务得到一次执行,后面每隔2秒执行一次（上一个任务的开始时间 + 延迟时间 = 下一个任务的开始时间）
//        executor.scheduleAtFixedDelay(FixedTask, 3000, 2000, TimeUnit.MILLISECONDS);
}
