package org.haobtc.onekey.onekeys.homepage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import com.chad.library.adapter.base.BaseQuickAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.adapter.SelectAccountCoinItemAdapter;
import org.haobtc.onekey.adapter.WalletListTypeAdapter;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.WalletInfo;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.event.GotPassEvent;
import org.haobtc.onekey.event.RefreshEvent;
import org.haobtc.onekey.onekeys.HomeOneKeyActivity;
import org.haobtc.onekey.onekeys.dialog.RecoverHdWalletActivity;
import org.haobtc.onekey.onekeys.homepage.mindmenu.HDWalletActivity;
import org.haobtc.onekey.onekeys.homepage.process.CreateLocalMainWalletActivity;
import org.haobtc.onekey.onekeys.walletprocess.createfasthd.CreateFastHDSoftWalletActivity;
import org.haobtc.onekey.onekeys.walletprocess.createsoft.CreateSoftWalletActivity;
import org.haobtc.onekey.ui.activity.SearchDevicesActivity;
import org.haobtc.onekey.ui.dialog.CreateWalletWaySelectorDialog;
import org.haobtc.onekey.ui.dialog.HdWalletIntroductionDialog;
import org.haobtc.onekey.utils.NavUtils;
import org.haobtc.onekey.viewmodel.AppWalletViewModel;
import org.haobtc.onekey.viewmodel.WalletListViewModel;

/** @author jinxiaomin */
public class WalletListActivity extends BaseActivity
        implements BaseQuickAdapter.OnItemChildClickListener {

    @BindView(R.id.recl_wallet_detail)
    LinearLayout reclWalletDetail;

    @BindView(R.id.recl_wallet_list)
    RecyclerView reclWalletList;

    @BindView(R.id.text_wallet_num)
    TextView textWalletNum;

    @BindView(R.id.view_all)
    ImageView viewAll;

    @BindView(R.id.view_hardware)
    ImageView viewHardware;

    @BindView(R.id.tet_None)
    TextView tetNone;

    @BindView(R.id.img_add)
    ImageView imgAdd;

    @BindView(R.id.text_wallet_type)
    TextView textWalletType;

    @BindView(R.id.img_w)
    ImageView imgW;

    @BindView(R.id.left_recyclerview)
    RecyclerView leftRecyclerView;

    private boolean isAddHd;
    private AppWalletViewModel mAppWalletViewModel;
    private WalletListTypeAdapter mAdapter;
    private WalletListViewModel mWalletModel;
    // 所有钱包的集合
    private final List<WalletInfo> mAllList = new ArrayList<>();
    // 显示钱包
    private int mAllNum;
    private final ArrayList<WalletInfo> coinList = new ArrayList<>();
    private final ArrayList<WalletInfo> hardwareList = new ArrayList<>();
    private SelectAccountCoinItemAdapter mSelectAccountCoinItemAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_wallet_list;
    }

    @Override
    public void initData() {
        mWalletModel = new ViewModelProvider(this).get(WalletListViewModel.class);
        reclWalletList.setNestedScrollingEnabled(false);
        mAdapter = new WalletListTypeAdapter(mAllList);
        initLeftCoinList();
        reclWalletList.setAdapter(mAdapter);
        mSelectAccountCoinItemAdapter.setOnItemClickListener(
                (adapter, view, position) -> {
                    List<Vm.CoinType> coinTypes = adapter.getData();
                    Vm.CoinType coinType = coinTypes.get(position);
                    textWalletType.setText(
                            String.format(
                                    Locale.ENGLISH,
                                    "%s%s",
                                    coinType.callFlag.toUpperCase(),
                                    getString(R.string.account)));
                    mWalletModel.getAllWallets(coinType.callFlag);
                    setOtherState();
                    mSelectAccountCoinItemAdapter.selectIndex(position);
                });
        mWalletModel.mAllWallets.observe(
                this,
                mAllData -> {
                    mAllNum = mAllData.validShowNum;
                    mAllList.addAll(mAllData.wallets);
                    mAdapter.setNewData(mAllData.wallets);
                });

        mWalletModel.mCoinWallets.observe(
                this,
                list -> {
                    if (list != null && list.size() >= 0)
                        textWalletNum.setText(String.valueOf(list.size()));
                    mAdapter.setNewData(list);
                });
        mWalletModel.mHardwareWallets.observe(this, hardwareList::addAll);
        mAdapter.setOnItemChildClickListener(this);
    }

    private void setOtherState() {
        textWalletNum.setVisibility(View.VISIBLE);
        reclWalletDetail.setVisibility(View.GONE);
        imgW.setVisibility(View.GONE);
        viewHardware.setImageDrawable(getDrawable(R.drawable.vector_hardware_dark));
        viewAll.setImageDrawable(getDrawable(R.drawable.vector_all_wallet_dark));
    }

    private void initLeftCoinList() {
        mSelectAccountCoinItemAdapter = new SelectAccountCoinItemAdapter();
        mSelectAccountCoinItemAdapter.selectIndex(-1);
        mSelectAccountCoinItemAdapter.setNewData(Arrays.asList(Vm.CoinType.values()));
        leftRecyclerView.setAdapter(mSelectAccountCoinItemAdapter);
    }

    @Override
    public void initView() {
        EventBus.getDefault().register(this);
        mAppWalletViewModel = getApplicationViewModel(AppWalletViewModel.class);
    }

    @SingleClick
    @SuppressLint("UseCompatLoadingForDrawables")
    @OnClick({
        R.id.img_close,
        R.id.recl_wallet_detail,
        R.id.lin_pair_wallet,
        R.id.lin_add_wallet,
        R.id.view_all,
        R.id.view_hardware,
        R.id.img_add,
        R.id.img_w
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_close:
                finish();
                break;
            case R.id.recl_wallet_detail:
                Intent intent4 = new Intent(WalletListActivity.this, HDWalletActivity.class);
                startActivity(intent4);
                break;
            case R.id.lin_pair_wallet:
                EventBus.getDefault().removeAllStickyEvents();
                Intent intent = new Intent(this, SearchDevicesActivity.class);
                intent.putExtra(
                        Constant.SEARCH_DEVICE_MODE,
                        Constant.SearchDeviceMode.MODE_PAIR_WALLET_TO_COLD);
                startActivity(intent);
                break;
            case R.id.lin_add_wallet:
                new CreateWalletWaySelectorDialog().show(getSupportFragmentManager(), "");
                break;
            case R.id.view_all:
                textWalletType.setText(getString(R.string.hd_wallet));
                viewAll.setImageDrawable(getDrawable(R.drawable.vector_all_wallet_light));
                reclWalletDetail.setVisibility(View.VISIBLE);
                textWalletNum.setVisibility(View.GONE);
                imgAdd.setVisibility(View.GONE);
                imgW.setVisibility(View.VISIBLE);
                mAdapter.setNewData(mAllList);
                mSelectAccountCoinItemAdapter.selectIndex(-1);
                viewHardware.setImageDrawable(getDrawable(R.drawable.vector_hardware_dark));
                break;
            case R.id.view_hardware:
                textWalletType.setText(getString(R.string.hardware_wallet_list));
                viewAll.setImageDrawable(getDrawable(R.drawable.vector_all_wallet_dark));
                viewHardware.setImageDrawable(getDrawable(R.drawable.vector_hardware_light));
                textWalletNum.setVisibility(View.VISIBLE);
                textWalletNum.setText(String.valueOf(Math.max(0, hardwareList.size() - 1)));
                reclWalletDetail.setVisibility(View.GONE);
                imgAdd.setVisibility(View.GONE);
                imgW.setVisibility(View.GONE);
                mAdapter.setNewData(hardwareList);
                mSelectAccountCoinItemAdapter.selectIndex(-1);
                break;
            case R.id.img_add:
                CreateSoftWalletActivity.start(this);
                break;
            case R.id.img_w:
                new HdWalletIntroductionDialog()
                        .show(getSupportFragmentManager(), "hd_introduction");
                break;
        }
    }

    private boolean shouldResponsePassEvent() {
        return (mAllNum == 0 && isAddHd);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGotPass(GotPassEvent event) {
        if (shouldResponsePassEvent()) {
            if (event.fromType == 1) {
                CreateLocalMainWalletActivity.start(this, event.getPassword());
            }
        }
    }

    @Subscribe
    public void onRefresh(RefreshEvent event) {
        if (shouldResponsePassEvent()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        int id = view.getId();
        if (id == R.id.recl_add_hd_wallet) {
            isAddHd = true;
            NavUtils.gotoSoftPassActivity(mContext, 1);
        } else if (id == R.id.recl_recovery_wallet) {
            isAddHd = false;
            Intent intent2 = new Intent(mContext, RecoverHdWalletActivity.class);
            startActivity(intent2);
        } else if (id == R.id.recl_add_wallet) {
            CreateFastHDSoftWalletActivity.start(this);
        } else if (id == R.id.rel_background) {
            WalletInfo data = (WalletInfo) adapter.getItem(position);
            String name = data.name;
            mAppWalletViewModel.submit(
                    () -> {
                        mAppWalletViewModel.changeCurrentWallet(name);
                        mIntent(HomeOneKeyActivity.class);
                    });
        } else if (id == R.id.recl_add_hardware_wallet) {
            EventBus.getDefault().removeAllStickyEvents();
            Intent intent = new Intent(this, SearchDevicesActivity.class);
            intent.putExtra(
                    Constant.SEARCH_DEVICE_MODE,
                    Constant.SearchDeviceMode.MODE_PAIR_WALLET_TO_COLD);
            startActivity(intent);
        }
    }
}
