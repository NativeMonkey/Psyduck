package com.psyduck.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.psyduck.app.R;
import com.psyduck.app.base.BaseActivity;
import com.psyduck.app.databinding.ActivityAddFriendBinding;

/**
 * Created by Administrator on 2017/4/5.
 */

public class AddFriendActivity extends BaseActivity<ActivityAddFriendBinding> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        setTitle("立即聊天");
        showContentView();
        bindingView.btnStartSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bindingView.etName.getText().toString().trim().equals("")) {
                    //参数为要添加的好友的username和添加理由
                    try {
                        EMClient.getInstance().contactManager().addContact(bindingView.etName.getText().toString().trim(), "");
                        startActivity(new Intent(getApplicationContext(), ChatActivity.class).putExtra("userId", bindingView.etName.getText().toString().trim()));
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
