package org.haobtc.onekey.event;

import org.haobtc.onekey.constant.Vm;

/**
 * @author liyan
 * @date 11/23/20
 */
public class GetXpubEvent {
    private Vm.CoinType coinType;

    public GetXpubEvent(Vm.CoinType coinType) {
        this.coinType = coinType;
    }

    public Vm.CoinType getCoinType() {
        return coinType;
    }
}
