package org.haobtc.onekey.manager;

import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.Map;
import org.haobtc.onekey.onekeys.homepage.FindFragment;
import org.haobtc.onekey.onekeys.homepage.MineFragment;
import org.haobtc.onekey.onekeys.homepage.SwapFragment;
import org.haobtc.onekey.onekeys.homepage.WalletFragment;
import org.haobtc.onekey.ui.base.BaseFragment;

public class MainFragmentFactory {

    private static Map<Integer, BaseFragment> mMap = new HashMap<>();

    public static Fragment createFragment(int position) {
        BaseFragment fragment = mMap.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new WalletFragment();
                    break;
                case 1:
                    fragment = new SwapFragment();
                    break;
                case 2:
                    fragment = new FindFragment();
                    break;
                case 3:
                    fragment = new MineFragment();
                    break;
            }
            mMap.put(position, fragment);
        }
        return fragment;
    }
}
