package com.psyduck.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.psyduck.app.Constant;
import com.psyduck.app.R;
import com.psyduck.app.application.MyApplication;
import com.psyduck.app.base.BaseFragment;
import com.psyduck.app.databinding.FragmentMsgBinding;
import com.psyduck.app.event.MsgEvent;
import com.psyduck.app.ui.activity.AddFriendActivity;
import com.psyduck.app.ui.activity.ChatActivity;
import com.psyduck.app.ui.activity.GoodFriendActivity;
import com.psyduck.app.util.UserInfoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/31.
 */

public class MsgFragment extends BaseFragment<FragmentMsgBinding> {

    @Override
    public int setContent() {
        return R.layout.fragment_msg;
    }

    private boolean mIsPrepared = false;
    private boolean mIsFirst = true;
    private final static int MSG_REFRESH = 2;

    protected List<EMConversation> conversationList = new ArrayList<EMConversation>();
    protected InputMethodManager inputMethodManager;

    protected ImageButton clearSearch;
    protected EditText query;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mIsPrepared = true;
        showContentView();
        initView();
        UserInfoUtils.putBoolean(getActivity().getApplicationContext(),"msg",true);
    }


    protected Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    onConnectionDisconnected();
                    break;
                case 1:
                    onConnectionConnected();
                    break;

                case MSG_REFRESH:
                    conversationList.clear();
                    conversationList.addAll(loadConversationList());
                    bindingView.list.refresh();
                    break;
                default:
                    break;
            }
        }
    };
    protected void onConnectionConnected(){
        bindingView.llErr.setVisibility(View.GONE);
    }
    protected void onConnectionDisconnected(){
        bindingView.llErr.setVisibility(View.VISIBLE);
    }

    private void initView() {
        bindingView.titleBar.setRightImageResource(R.mipmap.ic_add);
        bindingView.titleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), AddFriendActivity.class));
            }
        });
        bindingView.list.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        bindingView.titleBar.setLeftImageResource(R.mipmap.ic_good_friend);
        bindingView.titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), GoodFriendActivity.class));
            }
        });
        conversationList.addAll(loadConversationList());
        bindingView.list.init(conversationList);
        bindingView.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = bindingView.list.getItem(position);
                String username = conversation.conversationId();
                if (username.equals(EMClient.getInstance().getCurrentUser()))
                    Toast.makeText(getActivity(), R.string.Cant_chat_with_yourself, Toast.LENGTH_SHORT).show();
                else {
                    // start chat acitivity
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    if(conversation.isGroup()){
                        if(conversation.getType() == EMConversation.EMConversationType.ChatRoom){
                            // it's group chat
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_CHATROOM);
                        }else{
                            intent.putExtra(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_GROUP);
                        }

                    }
                    // it's single chat
                    intent.putExtra(Constant.EXTRA_USER_ID, username);
                    startActivity(intent);
                }
            }
        });
        query = (EditText) getActivity().findViewById(R.id.query);
        clearSearch = (ImageButton) getActivity().findViewById(R.id.search_clear);
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query.setText("");
            }
        });
    }

    protected List<EMConversation> loadConversationList(){
        // get all conversations
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        /**
         * lastMsgTime will change if there is new message during sorting
         * so use synchronized to make sure timestamp of last message won't change.
         */
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first.equals(con2.first)) {
                    return 0;
                } else if (con2.first.longValue() > con1.first.longValue()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }
    protected void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void refresh() {
        if(!handler.hasMessages(MSG_REFRESH)){
            handler.sendEmptyMessage(MSG_REFRESH);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgEvent(MsgEvent msgEvent) {
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
