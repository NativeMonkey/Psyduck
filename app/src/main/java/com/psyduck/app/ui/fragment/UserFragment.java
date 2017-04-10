package com.psyduck.app.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.psyduck.app.R;
import com.psyduck.app.application.MyApplication;
import com.psyduck.app.base.BaseFragment;
import com.psyduck.app.databinding.FragmentUserBinding;
import com.psyduck.app.oss.UploadListener;
import com.psyduck.app.ui.activity.LoginActivity;
import com.psyduck.app.ui.activity.RegisterActivity;
import com.psyduck.app.util.GlideImagePickerLoader;
import com.psyduck.app.util.OSSUtil;
import com.psyduck.app.util.UserInfoUtils;
import com.yixia.camera.demo.ui.record.FullScreenActivity;
import com.yixia.camera.demo.ui.record.MediaRecorderActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/31.
 */

public class UserFragment extends BaseFragment<FragmentUserBinding> {

    public static final String TAG = "UserFragment";

    @Override
    public int setContent() {
        return R.layout.fragment_user;
    }

    private boolean mIsPrepared = false;
    private boolean mIsFirst = true;

    private ImagePicker imagePicker;
    int width = 300;
    int height = 300;
    int saveX = 800;
    int saveY = 800;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIsPrepared = true;
        showContentView();
        initImagePicker();
        Glide.with(getActivity().getApplicationContext())//配置上下文
                .load(MyApplication.endPoint + "/" + MyApplication.bucketName + "/" + "CDN/image/" + UserInfoUtils.getString(getActivity().getApplicationContext(), "username") + ".jpg?"+System.currentTimeMillis())      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(R.mipmap.ic_user_bg)           //设置错误图片
                .placeholder(R.mipmap.default_image)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .into(bindingView.ivPhoto);
        bindingView.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), RegisterActivity.class));
            }
        });
        bindingView.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            }
        });
        bindingView.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EMClient.getInstance().logout(true, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        getActivity().finish();
                        Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onError(int code, String message) {
                        // TODO Auto-generated method stub
                        Log.e(TAG, "登出失败code" + code + "message" + message);
                    }
                });
            }
        });
        bindingView.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ImageGridActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        bindingView.btnRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), MediaRecorderActivity.class));
            }
        });
        bindingView.btnRecordingEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), FullScreenActivity.class));
            }
        });
    }

    private void initImagePicker() {
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImagePickerLoader());
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
        imagePicker.setFocusWidth(width);
        imagePicker.setFocusHeight(height);
        imagePicker.setOutPutX(saveX);
        imagePicker.setOutPutY(saveY);
        imagePicker.setShowCamera(true);
        imagePicker.setCrop(true);
        imagePicker.setMultiMode(false);
        imagePicker.setSaveRectangle(true);
    }

    @Override
    protected void loadData() {
        if (!mIsVisible || !mIsPrepared) {
            return;
        }
        if (!mIsFirst) {
            return;
        }
        mIsFirst = false;
    }

    @Override
    protected void onRefresh() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                final ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                Glide.with(getActivity().getApplicationContext())                             //配置上下文
                        .load(Uri.fromFile(new File(images.get(0).path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                        .error(R.mipmap.default_image)           //设置错误图片
                        .placeholder(R.mipmap.default_image)     //设置占位图片
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                        .into(bindingView.ivPhoto);
                OSSUtil.getInstance().upload(images.get(0).path, new UploadListener() {
                    @Override
                    public void uploadSucc(String url) {
                        Log.e(TAG, url);
                        MyApplication.getInstance().setSysTime(System.currentTimeMillis());
                    }

                    @Override
                    public void uploadFail(String msg) {
                        Log.e(TAG, msg);
                    }
                });

            }
        }
    }
}
