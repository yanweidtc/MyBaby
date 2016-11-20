package com.hdcy.app.basefragment;

import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hdcy.app.R;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * Created by WeiYanGeorge on 2016-09-28.
 */

public class BaseFragment extends SupportFragment {
    private static final String TAG = "Fragmentation";

    protected void initToolbarMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.hierarchy);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_hierarchy:
                        _mActivity.showFragmentStackHierarchyView();
                        _mActivity.logFragmentStackHierarchy(TAG);
                        break;
                }
                return true;
            }
        });
    }
}
