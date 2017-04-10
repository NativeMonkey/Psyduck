package com.psyduck.app.ui.activity;

import android.os.Bundle;
import com.psyduck.app.R;
import com.psyduck.app.base.BaseActivity;
import com.psyduck.app.databinding.ActivityGoodFriendBinding;
/**
 * Created by Administrator on 2017/4/5.
 */

public class GoodFriendActivity extends BaseActivity<ActivityGoodFriendBinding> {

    private static final String TAG = "GoodFriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_friend);
        setTitle("好友");
        showContentView();
    }
}
