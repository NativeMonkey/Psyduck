package com.psyduck.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.util.EasyUtils;
import com.psyduck.app.R;
import com.psyduck.app.domain.EmojiconExampleGroupData;
import com.psyduck.app.ui.fragment.ChatFragment;

/**
 * Created by Administrator on 2017/4/1.
 */

public class ChatActivity extends FragmentActivity {
    private EaseChatFragment chatFragment;
    String toChatUsername;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_chat);
        //get user id or group id
        toChatUsername = getIntent().getExtras().getString("userId");
        //use EaseChatFratFragment
        chatFragment = new ChatFragment();
        //pass parameters to chat fragment
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // make sure only one chat activity is opened
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
        if (EasyUtils.isSingleActivity(this)) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }
}
