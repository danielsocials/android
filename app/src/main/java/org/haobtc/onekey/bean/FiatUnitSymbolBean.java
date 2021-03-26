package org.haobtc.onekey.bean;

/**
 * 法币单位符号索引模型
 *
 * @author Onekey@QuincySx
 * @create 2021-01-08 11:28 AM
 */
public class FiatUnitSymbolBean {
    private String unit;
    private String symbol;

    public FiatUnitSymbolBean(String unit, String symbol) {
        this.unit = unit;
        this.symbol = symbol;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
