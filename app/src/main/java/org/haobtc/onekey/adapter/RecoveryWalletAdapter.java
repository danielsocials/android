package org.haobtc.onekey.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.BalanceInfoDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaomin
 */
public class RecoveryWalletAdapter extends RecyclerView.Adapter<RecoveryWalletAdapter.myViewHolder> {
    private Context context;
    private List<BalanceInfoDTO> walletList;
    private Map<Integer, Boolean> checkStatus;

    public RecoveryWalletAdapter(Context context, List<BalanceInfoDTO> walletList) {
        this.context = context;
        this.walletList = walletList;
        initData();
    }

    private void initData() {
        checkStatus = new HashMap<>();
        for (int i = 0; i < walletList.size(); i++) {
            checkStatus.put(i, true);//
        }
    }

    //Get the final map storage data
    public Map<Integer, Boolean> getMap() {
        return checkStatus;
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tetWalletName, textWalletBalance;
        CheckBox checkbox;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tetWalletName = itemView.findViewById(R.id.text_wallet_name);
            textWalletBalance = itemView.findViewById(R.id.text_wallet_balance);
            checkbox = itemView.findViewById(R.id.check_wallet);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.choose_hd_wallet_item, null);
        return new myViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        BalanceInfoDTO item = walletList.get(position);
        holder.tetWalletName.setText(item.getLabel());
        String strBalance;
        if (item.getWallets().size() > 0) {
            strBalance = item.getWallets().get(0).getBalance();
        } else {
            strBalance = "0";
        }

        String label = item.getLabel();
        if (!Strings.isNullOrEmpty(strBalance) && strBalance.contains("(")) {
            String balance = strBalance.substring(0, strBalance.indexOf("("));
            holder.textWalletBalance.setText(balance);
        } else {
            holder.textWalletBalance.setText(strBalance);
        }

        if ("BTC-1".equals(label) || "ETH-1".equals(label)) {
            holder.checkbox.setBackground(ContextCompat.getDrawable(context, R.drawable.gray_not_check));
            holder.checkbox.setChecked(true);
            holder.checkbox.setOnCheckedChangeListener(((buttonView, isChecked) ->
            {
                Toast.makeText(context, R.string.not_allow_un_check, Toast.LENGTH_SHORT).show();
            }));
        } else {
            holder.checkbox.setOnCheckedChangeListener(null);
            if (checkStatus.get(position) != null) {
                holder.checkbox.setChecked(checkStatus.get(position));
            }
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkStatus.put(position, isChecked);
                }
            });
            if (checkStatus.get(position) == null) {
                checkStatus.put(position, true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return walletList == null ? 0 : walletList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
