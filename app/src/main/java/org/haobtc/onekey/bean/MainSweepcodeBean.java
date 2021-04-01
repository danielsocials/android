package org.haobtc.onekey.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.haobtc.onekey.constant.Vm;

public class MainSweepcodeBean {

    /**
     * type : 1 data : {"amount":"0.005
     * BTC","message":"bcrt1qkhu5zfx7s5rms08efr0gfapty47h5dakl2twru3qn99yhck7g4hs0x3xrr","time":1591325822,"address":"bcrt1q82vs5lafxtr305utt62s8875s7df30vfnwnrjldtwsys8yjdtx2qslvf49","memo":"bcrt1qkhu5zfx7s5rms08efr0gfapty47h5dakl2twru3qn99yhck7g4hs0x3xrr"}
     */
    @SerializedName("type")
    private int type;

    @SerializedName("data")
    private DataBean data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        /**
         * amount : 0.005 BTC message :
         * bcrt1qkhu5zfx7s5rms08efr0gfapty47h5dakl2twru3qn99yhck7g4hs0x3xrr time : 1591325822
         * address : bcrt1q82vs5lafxtr305utt62s8875s7df30vfnwnrjldtwsys8yjdtx2qslvf49 memo :
         * bcrt1qkhu5zfx7s5rms08efr0gfapty47h5dakl2twru3qn99yhck7g4hs0x3xrr
         */
        @SerializedName("coin")
        private String coin;

        @SerializedName("amount")
        private String amount;

        @SerializedName("address")
        private String address;

        @SerializedName("selection")
        private List<SelectionBean> selection;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Vm.CoinType getCoin() {
            return Vm.CoinType.convertByCallFlag(coin);
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public List<SelectionBean> getSelection() {
            return selection;
        }

        public void setSelection(List<SelectionBean> selection) {
            this.selection = selection;
        }

        public Boolean isEmpty() {
            return coin == null || address == null;
        }
    }

    // {"coin": "btc", "prefix": "bitcoin", "format": "b58"}
    public static class SelectionBean {
        @SerializedName("coin")
        private String coin;

        @SerializedName("prefix")
        private String prefix;

        @SerializedName("format")
        private String format;

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Vm.CoinType getCoin() {
            return Vm.CoinType.convertByCallFlag(coin);
        }
    }
}
