package com.cqupt.driverbehaviourwarning;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.cqupt.driverbehaviourwarning.model.WarningMessage;
import com.cqupt.driverbehaviourwarning.service.TCPService;
import com.cqupt.driverbehaviourwarning.utils.MyOrientationListener;
import com.cqupt.driverbehaviourwarning.utils.SpeekerUtils;
import com.cqupt.driverbehaviourwarning.utils.ThreadPoolUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean isPermissionGranted = false;

    //显示Baidu地图
    private MapView mMapView = null;
    //定位相关
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    boolean isFirstLoc = true; // Is the first time positioning
    private MyLocationData locData;
    //获取定位信息并存储下来
    private MyOrientationListener myOrientationListener;
    private float mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    //防撞预警
    private LinearLayout anti_collision_linearlayout;
    private long AntiColli_MAX_LIMIT_TIME = 3000; // 防撞预警显示时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取权限
        if (!isPermissionGranted) {
            CheckPermission();
        }

        //显示Baidu地图
        init_BaiduMap();

        //开启Server Socket服务，等待连接
        startServer();

        ThreadPoolUtils.getInstance().getScheduledThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (MyApplication.lock) {
                        if (null == MyApplication.warningMessage) {
                            try {
                                MyApplication.lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        switch (MyApplication.warningMessage.getValue()) {
                            case "0":
                                SpeekerUtils.getInstance().speek(getApplicationContext(), "注意，驾驶员有不良驾驶行为。");
                                popupWarning(R.drawable.image_view_car_warning19001);
                                //popupWarning(R.drawable.g_image_view_car_warning19001, R.drawable.loading);
                                break;
                            case "1":
                                SpeekerUtils.getInstance().speek(getApplicationContext(), "危险，驾驶员有危险驾驶行为。");
                                popupWarning(R.drawable.image_view_car_warning19001);
                                break;
                            case "2":
                                SpeekerUtils.getInstance().speek(getApplicationContext(), "一切正常，请继续保持。");
                                popupWarning(R.drawable.image_view_car_warning19001);
                                break;
                        }
                        MyApplication.warningMessage = null;//清空
                    }
                }
            }
        });
    }

    /**
     * 初始化弹出窗口
     */
    private void initView() {
        anti_collision_linearlayout = (LinearLayout) findViewById(R.id.anti_collision_linearlayout);
        anti_collision_linearlayout.setVisibility(View.GONE);
    }

    /**
     * 弹出窗口（主线程）:图片
     * @param sourceID
     */
    void popupWarning(final int sourceID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (anti_collision_linearlayout.getVisibility() != View.VISIBLE) {
                    anti_collision_linearlayout.setBackgroundResource(sourceID);
                    anti_collision_linearlayout.setVisibility(View.VISIBLE);
                    anti_collision_linearlayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            anti_collision_linearlayout.setVisibility(View.GONE);
                        }
                    }, AntiColli_MAX_LIMIT_TIME);
                }
            }
        });
    }

    /**
     * 弹出窗口（主线程）:动图
     * @param gifURL 动图ID
     * @param placeHolder 占位图ID
     */
//    void popupWarning(final int gifURL, final int placeHolder) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (anti_collision_linearlayout.getVisibility() != View.VISIBLE) {
//                    anti_collision_linearlayout.setVisibility(View.VISIBLE);
//                    //加载动图
//                    Glide.with(anti_collision_linearlayout.getContext())
//                            .load(gifURL)
//                            .asGif()
//                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                            .placeholder(placeHolder)
//                            .error(placeHolder)
//                            .into(new ViewTarget<View, GlideDrawable>(anti_collision_linearlayout) {
//                                @Override
//                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
//                                    this.view.setBackground(resource.getCurrent());
//                                }
//                            });
//                    //显示时间
//                    anti_collision_linearlayout.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            anti_collision_linearlayout.setVisibility(View.GONE);
//                        }
//                    }, AntiColli_MAX_LIMIT_TIME);
//                }
//            }
//        });
//    }

    /**
     * 开启Server Socket，等待连接
     */
    private void startServer() {
        Intent iService = new Intent(this, TCPService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(iService);
    }

    /**
     * 关闭Server Socket
     */
    public void stopService() {
        Intent iService = new Intent(this, TCPService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stopService(iService);
    }

    /**
     * 初始化弹出窗口、Baidu地图，定位
     */
    private void init_BaiduMap() {
        //初始化弹出窗口
        initView();

        //get Baidu MapView and BaiduMap, Display Map
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);//Do not show scale controllers
        mBaiduMap = mMapView.getMap();

        //Set positioning mode
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        //Set positioning Marker as default
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));

        initLocationOption();
    }

    /**
     * set Location option
     */
    private void initLocationOption() {
        // init positioning
        mLocClient = new LocationClient(getApplicationContext());

        LocationClientOption option = new LocationClientOption();
        //Set the positioning mode to high precision mode
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll"); //Set the coordinate type
        option.setScanSpan(1000);//1 second positioning
        mLocClient.setLocOption(option);

        //Registered positioning listener function (Anonymous class)
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {

                //Map view is not processing new received locations after destruction
                if (bdLocation == null || mMapView == null) {
                    return;
                }
                //recording Latitude, Longitude, Radius
                mCurrentLat = bdLocation.getLatitude();
                mCurrentLon = bdLocation.getLongitude();
                mCurrentAccracy = bdLocation.getRadius();

                //Monitor direction sensor
                myOrientationListener = new MyOrientationListener(getApplicationContext());
                myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
                    @Override
                    public void onOrientationChanged(float x) {
                        //Assign the acquired x-axis direction to mCurrentDirection
                        mCurrentDirection = x;
                    }
                });
                //start direction sensor
                myOrientationListener.start();

                //The first positioning, the layer center moves to the location point
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());//中心点
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }

                //Structured positioning data(Real-time update positioning)
                locData = new MyLocationData.Builder().accuracy(mCurrentAccracy).direction(mCurrentDirection)
                        .latitude(mCurrentLat).longitude(mCurrentLon).build();
                mBaiduMap.setMyLocationData(locData);
            }
        });
    }

    /**
     * 弹出提示框：判断是否确定退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("系统提示")
                    .setMessage("确定要退出吗?")
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                        }
                    })
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            dialog.show();

            //设置弹窗样式：在dialog执行show之后才能来设置
            TextView tvMsg = (TextView) dialog.findViewById(android.R.id.message);
            tvMsg.setTextSize(16);
            tvMsg.setTextColor(Color.parseColor("#4E4E4E"));

            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextSize(16);
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#8C8C8C"));
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextSize(16);
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#1DA6DD"));

            try {
                Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                mAlert.setAccessible(true);
                Object alertController = mAlert.get(dialog);

                Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");
                mTitleView.setAccessible(true);

                TextView tvTitle = (TextView) mTitleView.get(alertController);
                if (null != tvTitle) {
                    tvTitle.setTextSize(16);
                    tvTitle.setTextColor(Color.parseColor("#000000"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * If Android SDK Version >= 6.0; need to request runtime permissions.
     */
    private void CheckPermission() {

        String[] needPermissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION, //Network positioning
                Manifest.permission.ACCESS_FINE_LOCATION,   //GPS positioning
                Manifest.permission.READ_PHONE_STATE,       //get phone state
                Manifest.permission.WRITE_EXTERNAL_STORAGE  //Used to write offline positioning data
        };

        //permissionList contains ungranted permissions
        List<String> permissionList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : needPermissions) {
                //Determine if the permission is GRANTED. If it is not, manually turns it on.
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }
            //Apply for permission manually
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, 1);
            } else {
                isPermissionGranted = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //地图生命周期管理
        mMapView.onResume();
        //Current activity is on the top of the stack, start positioning
        mBaiduMap.setMyLocationEnabled(true);
        //if all permissions granted, then start LocationClient.
        if (!mLocClient.isStarted() && isPermissionGranted) {
            mLocClient.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Current activity is not on the top of the stack, stop positioning
        mBaiduMap.setMyLocationEnabled(false);
        mLocClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //地图生命周期管理
        mMapView.onDestroy();
        //关闭Server Socket服务
        stopService();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) {
            Toast.makeText(this, "Horizontal screen", Toast.LENGTH_SHORT).show();
        } else if (Configuration.ORIENTATION_PORTRAIT == newConfig.orientation) {
            Toast.makeText(this, "Vertical screen", Toast.LENGTH_SHORT).show();
        }
    }
}
