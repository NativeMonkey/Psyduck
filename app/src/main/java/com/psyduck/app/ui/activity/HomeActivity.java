package com.psyduck.app.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.controller.EaseUI;
import com.jaeger.library.StatusBarUtil;
import com.psyduck.app.Constant;
import com.psyduck.app.R;
import com.psyduck.app.databinding.ActivityHomeBinding;
import com.psyduck.app.event.MsgEvent;
import com.psyduck.app.ui.fragment.HomeFragment;
import com.psyduck.app.ui.fragment.MsgFragment;
import com.psyduck.app.ui.fragment.ShopFragment;
import com.psyduck.app.ui.fragment.UserFragment;
import com.psyduck.app.util.UserInfoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class HomeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding activityHomeBinding;

    private int[] tabIcons = {R.drawable.home_bg, R.drawable.shop_bg, R.drawable.message_bg,
            R.drawable.user_bg};
    private Fragment[] fragments;
    private HomeFragment homeFragment;
    private ShopFragment shopFragment;
    private MsgFragment msgFragment;
    private UserFragment userFragment;
    private TextView [] textViews = new TextView[4];

    private boolean isExceptionDialogShow =  false;
    private AlertDialog.Builder exceptionBuilder;

    private int currentTabIndex;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        StatusBarUtil.setColor(HomeActivity.this, Color.parseColor("#ffcc66"),0);
        initView();
        UserInfoUtils.putBoolean(getApplicationContext(),"msg",false);
        homeFragment = new HomeFragment();
        shopFragment = new ShopFragment();
        msgFragment = new MsgFragment();
        userFragment = new UserFragment();
        fragments = new Fragment[] { homeFragment, shopFragment, msgFragment,userFragment};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment)
                .add(R.id.fragment_container, shopFragment).hide(shopFragment).show(homeFragment).commit();
        activityHomeBinding.rlHome.setOnClickListener(this);
        activityHomeBinding.rlShop.setOnClickListener(this);
        activityHomeBinding.rlMsg.setOnClickListener(this);
        activityHomeBinding.rlUser.setOnClickListener(this);
        if(null!=getIntent())
        showExceptionDialogFromIntent(getIntent());
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
        updateUnreadLabel();
    }

    private void initView()
    {
        textViews[0] = activityHomeBinding.tvHome;
        textViews[1] = activityHomeBinding.tvShop;
        textViews[2] = activityHomeBinding.tvMsg;
        textViews[3] = activityHomeBinding.tvUser;
        textViews[0].setSelected(true);
        textViews[0].setText(R.string.tab1_name);
        textViews[1].setText(R.string.tab2_name);
        textViews[2].setText(R.string.tab3_name);
        textViews[3].setText(R.string.tab4_name);
        textViews[0].setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[0], 0, 0);
        textViews[1].setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[1], 0, 0);
        textViews[2].setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[2], 0, 0);
        textViews[3].setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[3], 0, 0);
    }

    private static int getStatusBarHeight(Context context) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }


    private long mExitTime;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 800) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.rl_home:
                index = 0;
                break;
            case R.id.rl_shop:
                index = 1;
                break;
            case R.id.rl_msg:
                index = 2;
                break;
            case R.id.rl_user:
                index = 3;
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        textViews[currentTabIndex].setSelected(false);
        textViews[index].setSelected(true);
        currentTabIndex = index;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(null!=intent)
        showExceptionDialogFromIntent(intent);
    }

    private void showExceptionDialogFromIntent(Intent intent) {
        if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_CONFLICT, false)) {
            showExceptionDialog(Constant.ACCOUNT_CONFLICT);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_REMOVED, false)) {
            showExceptionDialog(Constant.ACCOUNT_REMOVED);
        } else if (!isExceptionDialogShow && intent.getBooleanExtra(Constant.ACCOUNT_FORBIDDEN, false)) {
            showExceptionDialog(Constant.ACCOUNT_FORBIDDEN);
        }
    }

    private void showExceptionDialog(String exceptionType) {
        isExceptionDialogShow = true;
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.e(TAG,"退出成功");
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                Log.e(TAG,"退出失败code"+code+"message"+message);
            }
        });
        String st = getResources().getString(R.string.Logoff_notification);
        if (!HomeActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (exceptionBuilder == null)
                    exceptionBuilder = new AlertDialog.Builder(HomeActivity.this);
                exceptionBuilder.setTitle(st);
                exceptionBuilder.setMessage(getExceptionMessageId(exceptionType));
                exceptionBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        exceptionBuilder = null;
                        isExceptionDialogShow = false;
                        finish();
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                exceptionBuilder.setCancelable(false);
                exceptionBuilder.create().show();
            } catch (Exception e) {
                Log.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }
        }
    }

    private int getExceptionMessageId(String exceptionType) {
        if(exceptionType.equals(Constant.ACCOUNT_CONFLICT)) {
            return R.string.connect_conflict;
        } else if (exceptionType.equals(Constant.ACCOUNT_REMOVED)) {
            return R.string.em_user_remove;
        } else if (exceptionType.equals(Constant.ACCOUNT_FORBIDDEN)) {
            return R.string.user_forbidden;
        }
        return R.string.Network_error;
    }

    EMMessageListener messageListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            // notify new message
            for (EMMessage message : messages) {
                EaseUI.getInstance().getNotifier().onNewMsg(message);
            }
            refreshUIWithMessage();
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {
        }

        @Override
        public void onMessageDelivered(List<EMMessage> message) {
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {}
    };

    private void refreshUIWithMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                // refresh unread count
                updateUnreadLabel();
                if (currentTabIndex == 2) {
                    // refresh conversation list
                    if (msgFragment != null) {
                        msgFragment.refresh();
                    }
                }else
                {
                    if(UserInfoUtils.getBoolean(getApplicationContext(),"msg"))
                    {
                        EventBus.getDefault().post(new MsgEvent());//当MsgFragment被创建之后调用
                    }
                }
            }
        });
    }
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            activityHomeBinding.unreadMsgNumber.setText(String.valueOf(count));
            activityHomeBinding.unreadMsgNumber.setVisibility(View.VISIBLE);
        } else {
            activityHomeBinding.unreadMsgNumber.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadLabel();
    }

    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        int chatroomUnreadMsgCount = 0;
        unreadMsgCountTotal = EMClient.getInstance().chatManager().getUnreadMessageCount();
        for(EMConversation conversation:EMClient.getInstance().chatManager().getAllConversations().values()){
            if(conversation.getType() == EMConversation.EMConversationType.ChatRoom)
                chatroomUnreadMsgCount=chatroomUnreadMsgCount+conversation.getUnreadMsgCount();
        }
        return unreadMsgCountTotal-chatroomUnreadMsgCount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
    }
}
