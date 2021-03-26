package org.haobtc.onekey.bean;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 钱包数字货币金额模型
 *
 * @author Onekey@QuincySx
 * @create 2021-01-08 10:47 AM
 */
public class AssetsBalance {

    private final BigDecimal balance;
    private final String unit;

    public AssetsBalance(@NonNull BigDecimal balance, @NonNull String unit) {
        this.balance = balance;
        this.unit = unit;
    }

    public AssetsBalance(@NonNull String balance, @NonNull String unit) {
        BigDecimal balanceBigDecimal;
        try {
            balanceBigDecimal = new BigDecimal(TextUtils.isEmpty(balance) ? "0" : balance);
        } catch (NumberFormatException e) {
            balanceBigDecimal = BigDecimal.ZERO;
        }
        this.balance = balanceBigDecimal;
        this.unit = unit;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getBalanceFormat(int scale) {
        // OPPO、ViVO 个别手机 balance 为 0 手动设置精度后，无法去掉后面的零。
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return balance.setScale(scale, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetsBalance that = (AssetsBalance) o;
        return balance.compareTo(that.balance) == 0 && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance, unit);
    }

    @Override
    public String toString() {
        return "AssetsBalance{" + "balance=" + balance + ", unit='" + unit + '\'' + '}';
    }
}
