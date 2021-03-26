package org.haobtc.onekey.ui.fragment;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.BackupWalletBean;
import org.haobtc.onekey.ui.base.BaseFragment;
import org.haobtc.onekey.ui.adapter.BackupWalletListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author liyan
 */
public class FindBackupFromPhoneFragment extends BaseFragment {

    @BindView(R.id.backup_list)
    protected RecyclerView mBackupListView;
    private List<BackupWalletBean> mBackupList;
    private BackupWalletListAdapter mAdapter;

    @Override
    public void init(View view) {
        mBackupList = new ArrayList<>();
        mBackupListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBackupListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_find_backup_from_phone;
    }

}
