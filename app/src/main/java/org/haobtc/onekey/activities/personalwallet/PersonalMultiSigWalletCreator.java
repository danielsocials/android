package org.haobtc.onekey.activities.personalwallet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yzq.zxinglibrary.common.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.activities.service.CommunicationModeSelector;
import org.haobtc.onekey.adapter.AddedXpubAdapter;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.XpubItem;
import org.haobtc.onekey.event.FirstEvent;
import org.haobtc.onekey.event.PersonalMutiSigEvent;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.utils.MyDialog;

public class PersonalMultiSigWalletCreator extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;

    @BindView(R.id.tet_personalNum)
    TextView tetPersonalNum;

    @BindView(R.id.recl_BinxinKey)
    RecyclerView reclBinxinKey;

    @BindView(R.id.bn_add_key)
    LinearLayout bnAddKey;

    @BindView(R.id.bn_complete_add_cosigner)
    Button bnCompleteAddCosigner;

    private int sigNum;
    public static final String TAG = PersonalMultiSigWalletCreator.class.getSimpleName();
    private String walletNames;
    private int walletNameNum;
    private ArrayList<XpubItem> addedXpubList;
    private SharedPreferences.Editor edit;
    private MyDialog myDialog;

    @SuppressLint("HandlerLeak")
    private Handler handler =
            new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1) {
                        ArrayList<String> pubList = new ArrayList<>();
                        for (int i = 0; i < addedXpubList.size(); i++) {
                            String keyaddress = addedXpubList.get(i).getXpub();
                            String deviceId = addedXpubList.get(i).getName();
                            pubList.add("[\"" + keyaddress + "\",\"" + deviceId + "\"]");
                        }
                        try {
                            PyEnv.sCommands.callAttr(
                                    "import_create_hw_wallet",
                                    walletNames,
                                    1,
                                    sigNum,
                                    pubList.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            myDialog.dismiss();
                            String message = e.getMessage();
                            if ("BaseException: file already exists at path".equals(message)) {
                                mToast(getString(R.string.changewalletname));
                            } else if (message.contains("The same xpubs have create wallet")) {
                                String haveWalletName =
                                        message.substring(message.indexOf("name=") + 5);
                                mToast(getString(R.string.xpub_have_wallet) + haveWalletName);
                            }
                            return;
                        }
                        edit.putInt("defaultName", walletNameNum);
                        edit.apply();
                        myDialog.dismiss();
                        EventBus.getDefault().post(new FirstEvent("11"));
                        Intent intent =
                                new Intent(
                                        PersonalMultiSigWalletCreator.this,
                                        CreatFinishPersonalActivity.class);
                        intent.putExtra("walletNames", walletNames);
                        intent.putExtra("flagTag", "onlyChoose");
                        intent.putExtra("strBixinlist", (Serializable) addedXpubList);
                        startActivity(intent);
                    }
                }
            };

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_only_choose;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        myDialog = MyDialog.showDialog(this);
        Intent intent = getIntent();
        sigNum = intent.getIntExtra("sigNum", 0);
        walletNames = intent.getStringExtra("walletNames");
        walletNameNum = intent.getIntExtra("walletNameNum", 0);
        init();
    }

    @SuppressLint("DefaultLocale")
    private void init() {
        tetPersonalNum.setText(
                String.format("%s(0/%d)", getString(R.string.creat_personal), sigNum));
    }

    @Override
    public void initData() {
        addedXpubList = new ArrayList<>();
    }

    @SingleClick
    @OnClick({R.id.img_backCreat, R.id.bn_add_key, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_add_key:
                // new version code
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(null);
                Intent intent1 = new Intent(this, CommunicationModeSelector.class);
                intent1.putExtra("tag", TAG);
                startActivity(intent1);
                break;
            case R.id.bn_complete_add_cosigner:
                handler.sendEmptyMessage(1);
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(PersonalMutiSigEvent event) {
        String xpub = event.getXpub();
        String deviceId = event.getDeviceId();
        String label = event.getLabel();
        showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub, deviceId, label);
    }

    private void showConfirmPubDialog(
            Context context, @LayoutRes int resource, String xpub, String deviceId, String label) {
        // set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);
        TextView editBixinName = view.findViewById(R.id.name);
        TextView tetNum = view.findViewById(R.id.txt_textNum);
        TextView textView = view.findViewById(R.id.xpub_info);
        textView.setText(xpub);
        if (!TextUtils.isEmpty(label)) {
            editBixinName.setText(label);
        } else {
            editBixinName.setText(getString(R.string.BixinKey));
        }
        editBixinName.addTextChangedListener(
                new TextWatcher() {
                    CharSequence input;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        input = s;
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        tetNum.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                        if (input.length() > 19) {
                            Toast.makeText(
                                            PersonalMultiSigWalletCreator.this,
                                            R.string.moreinput_text,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
        view.findViewById(R.id.btn_confirm)
                .setOnClickListener(
                        v -> {
                            String strBixinname = editBixinName.getText().toString();
                            if (TextUtils.isEmpty(strBixinname)) {
                                mToast(getString(R.string.input_name));
                                return;
                            }
                            if (TextUtils.isEmpty(xpub)) {
                                mToast(getString(R.string.input_public_address));
                                return;
                            }
                            addedXpubList.add(new XpubItem(strBixinname, xpub));
                            // bixinKEY
                            AddedXpubAdapter addedXpubAdapter = new AddedXpubAdapter(addedXpubList);
                            reclBinxinKey.setAdapter(addedXpubAdapter);
                            tetPersonalNum.setText(
                                    String.format(
                                            getString(R.string.creat_personal) + "(%d/%d)",
                                            addedXpubList.size(),
                                            sigNum));

                            if (addedXpubList.size() == sigNum) {
                                bnCompleteAddCosigner.setEnabled(true);
                                bnCompleteAddCosigner.setBackground(
                                        getDrawable(R.drawable.little_radio_blue));
                                bnAddKey.setVisibility(View.GONE);
                            }
                            addedXpubAdapter.setOnItemChildClickListener(
                                    new BaseQuickAdapter.OnItemChildClickListener() {
                                        @Override
                                        public void onItemChildClick(
                                                BaseQuickAdapter adapter, View view, int position) {
                                            if (view.getId() == R.id.img_deleteKey) {
                                                try {
                                                    PyEnv.sCommands.callAttr("delete_xpub", xpub);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                addedXpubList.remove(position);
                                                addedXpubAdapter.notifyDataSetChanged();
                                                bnAddKey.setVisibility(View.VISIBLE);
                                                tetPersonalNum.setText(
                                                        String.format(
                                                                getString(R.string.creat_personal)
                                                                        + "(%d/%d)",
                                                                addedXpubList.size(),
                                                                sigNum));
                                                bnCompleteAddCosigner.setBackground(
                                                        getDrawable(R.drawable.little_radio_qian));
                                                bnCompleteAddCosigner.setEnabled(false);
                                            }
                                        }
                                    });
                            dialogBtoms.cancel();
                        });
        // cancel dialog
        view.findViewById(R.id.img_cancel)
                .setOnClickListener(
                        v -> {
                            dialogBtoms.cancel();
                        });
        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        // set pop_up size
        assert window != null;
        window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // set locate
        window.setGravity(Gravity.BOTTOM);
        // set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
