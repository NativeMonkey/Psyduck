package com.psyduck.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.psyduck.app.R;
import com.psyduck.app.base.BaseFragment;
import com.psyduck.app.databinding.FragmentHomeBinding;

/**
 * Created by Administrator on 2017/3/31.
 */

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    @Override
    public int setContent() {
        return R.layout.fragment_home;
    }

    private boolean mIsPrepared = false;
    private boolean mIsFirst = true;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIsPrepared = true;
        showContentView();
        showEmpty();
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
}
