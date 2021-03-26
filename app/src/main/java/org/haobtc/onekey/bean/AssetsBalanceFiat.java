package org.haobtc.onekey.bean;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * 数字钱包法币金额模型
 *
 * @author Onekey@QuincySx
 * @create 2021-01-08 10:47 AM
 */
public class AssetsBalanceFiat {

    private final String balance;
    private final String unit;
    private final String symbol;

    public static AssetsBalanceFiat fromAmountAndUnit(
            @NonNull String balanceAndUnit, @NonNull String symbol) {
        String[] s = balanceAndUnit.split(" ");

        String balance;
        if (s.length > 0) {
            balance = s[0].trim();
        } else {
            balance = AssetsKt.DEF_WALLET_FIAT_BALANCE.balance;
        }

        String unit;
        if (s.length > 1) {
            unit = s[1].trim();
        } else {
            unit = AssetsKt.DEF_WALLET_FIAT_BALANCE.unit;
        }
        return new AssetsBalanceFiat(balance, unit, symbol);
    }

    public AssetsBalanceFiat(
            @NonNull String balance, @NonNull String unit, @NonNull String symbol) {
        this.balance =
                TextUtils.isEmpty(balance)
                        ? AssetsKt.DEF_WALLET_FIAT_BALANCE.getBalance()
                        : balance;
        this.unit = unit;
        this.symbol = symbol;
    }

    public String getBalance() {
        return balance;
    }

    public String getBalanceFormat() {
        if (TextUtils.isEmpty(balance) || balance.equals("0")) {
            return "0.00";
        }
        return balance;
    }

    public String getUnit() {
        return unit;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetsBalanceFiat that = (AssetsBalanceFiat) o;
        return Objects.equals(balance, that.balance)
                && Objects.equals(unit, that.unit)
                && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance, unit, symbol);
    }

    @Override
    public String toString() {
        return "AssetsBalanceFiat{"
                + "balance='"
                + balance
                + '\''
                + ", unit='"
                + unit
                + '\''
                + ", symbol='"
                + symbol
                + '\''
                + '}';
    }
}
