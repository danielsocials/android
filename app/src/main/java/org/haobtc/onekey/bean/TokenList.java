package org.haobtc.onekey.bean;

import java.util.List;

/** @Description: TokenList 的实体类 @Author: peter Qin */
public class TokenList {

    public String chain;
    public long timestamp;

    public List<ERCToken> tokens;

    public static class ERCToken {
        public String address;
        public int chainId;
        public String name;
        public String symbol;
        public String cgk_id;
        public int decimals;
        public String logoURI;
        public int rank;
        public boolean isAdd;
    }
}
