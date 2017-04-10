package com.psyduck.app.application;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.NetUtils;
import com.psyduck.app.Constant;
import com.psyduck.app.ui.activity.HomeActivity;
import com.yixia.camera.VCamera;
import com.yixia.camera.demo.VCameraDemoApplication;
import com.yixia.camera.demo.service.AssertService;
import com.yixia.camera.util.DeviceUtils;
import java.io.File;

/**
 * Created by Lilu on 2017/3/31.
 */

public class MyApplication extends VCameraDemoApplication {
    public static final String TAG = "MyApplication";
    private static MyApplication myApplication;
    private EMConnectionListener connectionListener;
    //oss阿里云存储
    private static final String accessKeyId = "1B7sjWW5nwg6srnv";
    private static final String accessKeySecret = "qLcYJD8sUnehCXeVY6hjbrsGFyIyEX";
    public static final String bucketName = "flashfish";
    public static final String endPoint ="http://oss-cn-hangzhou.aliyuncs.com";
    public static OSSClient oss;
    private long sysTime;

    public void setSysTime(long sysTime) {
        this.sysTime = sysTime;
    }

    public static MyApplication getInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        initIm();
        initListener();
        initOSS();
        sysTime = System.currentTimeMillis();
    }

    private void initIm() {
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        //初始化
        EMClient.getInstance().init(this, options);
        EaseUI.getInstance().init(this,options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
    }
    private void initListener()
    {
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                EMLog.d("global listener", "onDisconnect" + error);
                if (error == EMError.USER_REMOVED) {
                    onUserException(Constant.ACCOUNT_REMOVED);
                    Log.e(TAG,"显示帐号已经被移除");
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onUserException(Constant.ACCOUNT_CONFLICT);
                    Log.e(TAG,"显示帐号在其他设备登录");
                } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
                    onUserException(Constant.ACCOUNT_FORBIDDEN);
                    Log.e(TAG,"服务器限制");
                } else {
                    if (NetUtils.hasNetwork(myApplication)) {
                        Log.e(TAG,"连接不到聊天服务器");
                    }
                    else {
                        Log.e(TAG,"当前网络不可用，请检查网络设置");
                    }
                }
            }

            @Override
            public void onConnected() {
                Log.e(TAG,"连接正常");
                EaseUI.getInstance().setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
                    @Override
                    public EaseUser getUser(String username) {
                        EaseUser user = null;
                        if(username.equals(EMClient.getInstance().getCurrentUser())) {
                            user = new EaseUser(username);
                            user.setAvatar(MyApplication.endPoint+"/"+MyApplication.bucketName+"/"+"CDN/image/"+username+".jpg?"+sysTime);
                        }
                        if(user == null){
                            user = new EaseUser(username);
                            user.setAvatar(MyApplication.endPoint+"/"+MyApplication.bucketName+"/"+"CDN/image/"+username+".jpg?"+sysTime);
                        }
                        return user;
                    }
                });
            }
        };
        EMClient.getInstance().addConnectionListener(connectionListener);
    }

    private void initOSS()
    {
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSSLog.enableLog();
        oss = new OSSClient(getApplicationContext(), endPoint, credentialProvider, conf);
    }

    private void onUserException(String error)
    {
        Log.e(TAG,error);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(error, true);
        startActivity(intent);
    }
}
