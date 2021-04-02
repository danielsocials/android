package org.haobtc.onekey.onekeys.dappbrowser.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.aop.ClickUtil;
import org.haobtc.onekey.bean.CurrentFeeDetails;
import org.haobtc.onekey.bean.WalletAccountInfo;
import org.haobtc.onekey.business.wallet.SystemConfigManager;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.onekeys.dappbrowser.bean.Signable;
import org.haobtc.onekey.onekeys.dappbrowser.bean.Web3Transaction;
import org.haobtc.onekey.onekeys.dappbrowser.callback.DappActionSheetCallback;
import org.haobtc.onekey.onekeys.dappbrowser.callback.SignAuthenticationCallback;
import org.haobtc.onekey.onekeys.homepage.process.TransactionCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.web3j.utils.Convert;

/**
 * @author Onekey@QuincySx
 * @create 2021-03-03 10:44 AM
 */
public class DappActionSheetDialog extends BottomSheetDialog
        implements StandardFunctionInterface, DappActionSheetInterface {

    private final TextView balance;
    private final TextView amount;

    private final ImageView cancelButton;
    private final Button nextButton;

    private final View layoutProgress;
    private final View layoutHardwareProgress;

    private Web3Transaction candidateTransaction;
    private CurrentFeeDetails mCurrentFeeDetails;
    private @FeeType Integer mFeeType;
    private final DappActionSheetCallback actionSheetCallback;
    private SignAuthenticationCallback signCallback;
    private ActionSheetMode mode;
    private final long callbackId;

    private String txHash = null;
    private boolean actionCompleted;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private CurrentCoinTypeProvider mCurrentCoinTypeProvider;
    private final SystemConfigManager mSystemConfigManager =
            new SystemConfigManager(MyApplication.getInstance());

    /**
     * 处理交易签字
     *
     * @param activity dialog 依赖的 Activity
     * @param tx 交易
     * @param aCallBack Dapp 事件操作回调
     */
    public DappActionSheetDialog(
            @NonNull Activity activity,
            Web3Transaction tx,
            WalletAccountInfo wallet,
            DappActionSheetCallback aCallBack,
            CurrentCoinTypeProvider coinTypeProvider) {
        super(activity, R.style.BottomSheetDialogTheme);
        setContentView(R.layout.dialog_dapp_action_sheet);
        View delegate = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (delegate != null) {
            delegate.setBackgroundColor(Color.TRANSPARENT);
        }
        mCurrentCoinTypeProvider = coinTypeProvider;
        layoutProgress = findViewById(R.id.layout_progress);
        layoutHardwareProgress = findViewById(R.id.layout_hardware_progress);
        balance = findViewById(R.id.text_balance);
        amount = findViewById(R.id.text_tx_amount);
        TextView txFee = findViewById(R.id.text_tx_fee);

        TextView walletNameTextView = findViewById(R.id.text_send_name);
        walletNameTextView.setText(wallet.getName());

        TextView sendAddressTextView = findViewById(R.id.text_send_address);
        sendAddressTextView.setText(wallet.getAddress());

        TextView receiveAddressTextView = findViewById(R.id.text_receive_address);
        receiveAddressTextView.setText(tx.recipient.toString());

        BigDecimal bigDecimal = Convert.fromWei(new BigDecimal(tx.value), Convert.Unit.ETHER);
        amount.setText(
                bigDecimal.stripTrailingZeros().toPlainString()
                        + " "
                        + mSystemConfigManager.getCurrentBaseUnit(
                                coinTypeProvider.currentCoinType()));

        nextButton = findViewById(R.id.btn_confirm_pay);
        cancelButton = findViewById(R.id.img_cancel);
        mode = ActionSheetMode.SEND_TRANSACTION;

        signCallback = null;

        actionSheetCallback = aCallBack;
        actionCompleted = false;

        candidateTransaction = tx;
        callbackId = tx.leafPosition;

        fillFeeTextView();

        setupCancelListeners();
        setupNextListener();
        txFee.setOnClickListener(
                v -> {
                    DappFeeCustomSheetDialog dappFeeCustomSheetDialog =
                            DappFeeCustomSheetDialog.newInstance(
                                    mCurrentCoinTypeProvider.currentCoinType(),
                                    tx.recipient.toString(),
                                    tx.value.toString(),
                                    tx.payload);
                    dappFeeCustomSheetDialog.setOnFeeSelectCallback(
                            new DappFeeCustomSheetDialog.OnFeeSelectCallback() {
                                @Override
                                public void onInit(@NotNull OnInitDataInterface init) {
                                    init.onDataInit(
                                            mFeeType,
                                            mCurrentFeeDetails,
                                            tx.gasLimit,
                                            new BigDecimal(tx.gasPrice));
                                }

                                @Override
                                public void onSelectFee(
                                        @FeeType int feeType,
                                        @NotNull BigInteger gasPrice,
                                        @NotNull BigInteger gas) {
                                    mFeeType = feeType;
                                    candidateTransaction.gasLimit = gas;
                                    candidateTransaction.gasPrice = gasPrice;
                                    fillFeeTextView();
                                }
                            });
                    dappFeeCustomSheetDialog.show(
                            ((AppCompatActivity) activity).getSupportFragmentManager(), "Ssss");
                });
    }

    /**
     * 处理消息签字
     *
     * @param activity dialog 依赖的 Activity
     * @param aCallback Dapp 事件操作回调
     * @param sCallback 签字回调
     * @param message 消息
     */
    public DappActionSheetDialog(
            @NonNull Activity activity,
            WalletAccountInfo wallet,
            DappActionSheetCallback aCallback,
            SignAuthenticationCallback sCallback,
            Signable message) {
        super(activity, R.style.BottomSheetDialogTheme);
        setContentView(R.layout.dialog_dapp_action_sheet_sign);

        View delegate = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (delegate != null) {
            delegate.setBackgroundColor(Color.TRANSPARENT);
        }

        layoutProgress = findViewById(R.id.layout_progress);
        layoutHardwareProgress = findViewById(R.id.layout_hardware_progress);

        TextView walletNameTextView = findViewById(R.id.text_send_name);
        walletNameTextView.setText(wallet.getName());

        TextView sendAddressTextView = findViewById(R.id.text_send_address);
        sendAddressTextView.setText(wallet.getAddress());

        TextView messageTextView = findViewById(R.id.text_receive_address);
        messageTextView.setText(message.getMessage());
        messageTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        nextButton = findViewById(R.id.btn_confirm_pay);
        nextButton.setEnabled(true);

        cancelButton = findViewById(R.id.img_cancel);
        balance = null;
        amount = null;
        mode = ActionSheetMode.SIGN_MESSAGE;
        callbackId = message.getCallbackId();

        actionSheetCallback = aCallback;
        signCallback = sCallback;

        candidateTransaction = null;
        actionCompleted = false;

        setupCancelListeners();
        setupNextListener();
    }

    public void enableNextButton() {
        nextButton.setEnabled(true);
    }

    public void setFeeDetails(@Nullable CurrentFeeDetails currentFeeDetails, boolean recommend) {
        enableNextButton();
        mCurrentFeeDetails = currentFeeDetails;
        if (recommend) {
            mFeeType = FeeType.NORMAL;
        }
        fillFeeTextView();
    }

    private void fillFeeTextView() {
        TextView txFee = findViewById(R.id.text_tx_fee);
        BigDecimal feeBigDecimal =
                Convert.fromWei(
                        new BigDecimal(candidateTransaction.gasLimit)
                                .multiply(new BigDecimal(candidateTransaction.gasPrice)),
                        Convert.Unit.ETHER);
        txFee.setText(
                feeBigDecimal.stripTrailingZeros().toPlainString()
                        + " "
                        + mSystemConfigManager.getCurrentBaseUnit(
                                mCurrentCoinTypeProvider.currentCoinType()));
    }

    public void setSignOnly() {
        // sign only, and return signature to process
        mode = ActionSheetMode.SIGN_TRANSACTION;
    }

    public void onDestroy() {}

    public void setURL(String url) {}

    private void setNewBalanceText() {}

    private boolean isSendingTransaction() {
        return (mode == ActionSheetMode.SEND_TRANSACTION
                || mode == ActionSheetMode.SEND_TRANSACTION_DAPP
                || mode == ActionSheetMode.SEND_TRANSACTION_WC
                || mode == ActionSheetMode.SIGN_TRANSACTION);
    }

    @Override
    public void handleClick(String action, int id) {
        switch (mode) {
            case SEND_TRANSACTION_WC:
            case SEND_TRANSACTION:
            case SEND_TRANSACTION_DAPP:
                // check gas and warn user
                if (!checkSufficientGas()) {
                    askUserForInsufficientGasConfirm();
                } else {
                    sendTransaction();
                }
                break;
            case SIGN_MESSAGE:
                signMessage();
                break;
            case SIGN_TRANSACTION:
                signTransaction();
                break;
        }

        actionSheetCallback.notifyConfirm(mode.toString());
    }

    /**
     * 检查 Gas 费是否充足
     *
     * @return
     */
    private boolean checkSufficientGas() {
        return true;
    }

    private void signMessage() {
        // authentication screen
        SignAuthenticationCallback localSignCallback =
                new SignAuthenticationCallback() {
                    @Override
                    public void gotAuthorisation(String pwd, boolean gotAuth) {
                        actionCompleted = true;
                        // display success and hand back to calling function
                        signCallback.gotAuthorisation(pwd, gotAuth);
                    }

                    @Override
                    public void cancelAuthentication() {
                        signCallback.gotAuthorisation("", false);
                    }
                };

        actionSheetCallback.getAuthorisation(localSignCallback);
    }

    /**
     * Popup a dialogbox to ask user if they really want to try to send this transaction, as we
     * calculate it will fail due to insufficient gas. User knows best though.
     */
    private void askUserForInsufficientGasConfirm() {
        DappResultAlertDialog dialog = new DappResultAlertDialog(getContext());
        dialog.setIcon(DappResultAlertDialog.WARNING);
        dialog.setTitle(R.string.wallet_insufficient);
        dialog.setMessage(getContext().getString(R.string.wallet_insufficient));
        dialog.setButtonText(R.string.send);
        dialog.setSecondaryButtonText(R.string.cancel);
        dialog.setButtonListener(
                v -> {
                    dialog.dismiss();
                    sendTransaction();
                });
        dialog.setSecondaryButtonListener(
                v -> {
                    dialog.dismiss();
                });
        dialog.show();
    }

    public void transactionWritten(String tx) {
        txHash = tx;
        // dismiss on message completion
        showTransactionSuccess();
    }

    private Vm.CoinType getCurrentCoinType() {
        if (mCurrentCoinTypeProvider != null) {
            return mCurrentCoinTypeProvider.currentCoinType();
        } else {
            throw new RuntimeException("Please set up setCurrentCoinProvider");
        }
    }

    private void showTransactionSuccess() {
        switch (mode) {
            case SEND_TRANSACTION:
                // Display transaction success dialog
                TransactionCompletion.start(
                        getContext(),
                        getCurrentCoinType(),
                        txHash,
                        String.format(Locale.ENGLISH, "%s %s", amount, "" /* Unit */));
                dismiss();
                break;

            case SEND_TRANSACTION_WC:
            case SEND_TRANSACTION_DAPP:
                // return to dapp
                dismiss();
                break;

            case SIGN_TRANSACTION:
                dismiss();
                break;
        }
    }

    private void setupNextListener() {
        nextButton.setOnClickListener(
                v -> {
                    if (!ClickUtil.isFastDoubleClick(v, 300)) {
                        handleClick(null, 0);
                    }
                });
    }

    private void setupCancelListeners() {
        cancelButton.setOnClickListener(
                v -> {
                    dismiss();
                });

        setOnDismissListener(
                v -> {
                    actionSheetCallback.dismissed(txHash, callbackId, actionCompleted);
                });
    }

    private void signTransaction() {
        // get approval and push transaction
        // authentication screen
        signCallback =
                new SignAuthenticationCallback() {
                    @Override
                    public void gotAuthorisation(String pwd, boolean gotAuth) {
                        actionCompleted = true;
                        // send the transaction
                        actionSheetCallback.signTransaction(pwd, formTransaction());
                    }

                    @Override
                    public void cancelAuthentication() {}
                };
        actionSheetCallback.getAuthorisation(signCallback);
    }

    public void completeSignRequest(String pwd, boolean gotAuth) {
        if (signCallback != null) {
            actionCompleted = true;

            switch (mode) {
                case SEND_TRANSACTION_WC:
                case SEND_TRANSACTION:
                case SEND_TRANSACTION_DAPP:
                    signCallback.gotAuthorisation(pwd, gotAuth);
                    break;

                case SIGN_MESSAGE:
                    actionCompleted = true;
                    // display success and hand back to calling function
                    signCallback.gotAuthorisation(pwd, gotAuth);
                    break;

                case SIGN_TRANSACTION:
                    signCallback.gotAuthorisation(pwd, gotAuth);
                    break;
            }
        }
    }

    private Web3Transaction formTransaction() {
        // form Web3Transaction
        // get user gas settings
        return new Web3Transaction(
                candidateTransaction.recipient,
                candidateTransaction.contract,
                candidateTransaction.value,
                candidateTransaction.gasPrice,
                candidateTransaction.gasLimit,
                candidateTransaction.nonce,
                candidateTransaction.payload,
                candidateTransaction.leafPosition);
    }

    private void sendTransaction() {

        // get approval and push transaction
        // authentication screen
        signCallback =
                new SignAuthenticationCallback() {
                    @Override
                    public void gotAuthorisation(String pwd, boolean gotAuth) {
                        actionCompleted = true;
                        // send the transaction
                        actionSheetCallback.sendTransaction(pwd, formTransaction());
                    }

                    @Override
                    public void cancelAuthentication() {}
                };

        actionSheetCallback.getAuthorisation(signCallback);
    }

    @Override
    public void lockDragging(boolean lock) {
        getBehavior().setDraggable(!lock);

        // ensure view fully expanded when locking scroll. Otherwise we may not be able to see our
        // expanded view
        if (lock) {
            FrameLayout bottomSheet =
                    findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    public void success() {
        dismiss();
    }

    public void showProgress() {
        mHandler.postAtFrontOfQueue(
                () -> {
                    layoutProgress.setVisibility(View.VISIBLE);
                });
    }

    public void showHardwareProgress() {
        mHandler.postAtFrontOfQueue(
                () -> {
                    layoutHardwareProgress.setVisibility(View.VISIBLE);
                });
    }

    public void hideProgress() {
        mHandler.post(
                () -> {
                    layoutProgress.setVisibility(View.GONE);
                    layoutHardwareProgress.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
