package com.psyduck.app.oss;

/**
 * Created by Administrator on 2017/2/21.
 */

public interface UploadListener {
    void uploadSucc(String url);
    void uploadFail(String msg);
}
