package com.psyduck.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.psyduck.app.domain.EmojiconExampleGroupData;

import java.util.Map;

/**
 * Created by Administrator on 2017/4/6.
 */

public class ChatFragment extends EaseChatFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void setUpView() {
        super.setUpView();
        EaseUI.getInstance().setEmojiconInfoProvider(new EaseUI.EaseEmojiconInfoProvider() {
            @Override
            public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                for(EaseEmojicon emojicon : data.getEmojiconList()){
                    if(emojicon.getIdentityCode().equals(emojiconIdentityCode)){
                        return emojicon;
                    }
                }
                return null;
            }

            @Override
            public Map<String, Object> getTextEmojiconMapping() {
                return null;
            }
        });
        ((EaseEmojiconMenu)inputMenu.getEmojiconMenu()).addEmojiconGroup(EmojiconExampleGroupData.getData());
    }
}
