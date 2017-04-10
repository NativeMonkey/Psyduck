package com.psyduck.app.util;

import com.psyduck.app.application.MyApplication;
import com.psyduck.app.oss.PutObjectSamples;
import com.psyduck.app.oss.UploadListener;

/**
 * Created by Administrator on 2017/2/21.
 */

public class OSSUtil {
    private static final String TAG = "OSSUtil";

    private OSSUtil(){}
    private static OSSUtil ossUtil = null;
    public static OSSUtil getInstance()
    {
        if(ossUtil == null)
        {
            ossUtil = new OSSUtil();
        }
        return ossUtil;
    }
    public void upload(final String path,final UploadListener uploadListener)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new PutObjectSamples(MyApplication.oss, MyApplication.bucketName,"CDN/image/"+UserInfoUtils.getString(MyApplication.getInstance(),"username")+".jpg", path, uploadListener).asyncPutObjectFromLocalFile();
            }
        }).start();
    }
}
