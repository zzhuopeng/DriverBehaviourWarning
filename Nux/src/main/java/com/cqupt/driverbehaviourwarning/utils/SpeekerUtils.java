package com.cqupt.driverbehaviourwarning.utils;

import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 内部静态类，单例模式
 */
public class SpeekerUtils {
    private static final String TAG = "SpeekerUtils";

    private SpeechSynthesizer mTts;
    //防止不正常播报
    private volatile boolean speakSwitch = true;

    /**
     * 私有化构造方法
     */
    private SpeekerUtils() {
    }

    /**
     * 静态工厂方法获取单例对象
     *
     * @return 语音工具类
     */
    public static SpeekerUtils getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 内部静态类：实例化 科大讯飞语音合成对象
     */
    private static class SingletonHolder {
        private static final SpeekerUtils instance = new SpeekerUtils();
    }

    /**
     * 获取 播报者
     *
     * @return 语音合成器
     */
    public void speak(Context context, final String string) {
        if (null == mTts) {
            mTts = initSpeekerUtils(context);
        }
        //打开情况下，才播报语音
        if (speakSwitch) {
            //播报语音
            mTts.startSpeaking(string, mSynthesizerListener);
        }
    }

    /**
     * 播报状态监听器
     */
    private SynthesizerListener mSynthesizerListener = new com.iflytek.cloud.SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            speakSwitch = false; //播放开始时，关闭开关。
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            speakSwitch = true; //播放完成后，打开开关。
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


    /**
     * 初始化SpeekerUtils配置
     */
    private SpeechSynthesizer initSpeekerUtils(Context context) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(context, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
        //mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        return mTts;
    }
}
