package org.haobtc.onekey.utils;

import android.text.TextUtils;
import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.event.FirstEvent;
import org.haobtc.onekey.event.SecondEvent;

public class Daemon {
    private static volatile Daemon daemon;

    private Daemon() {}

    public static Daemon getInstance() {
        if (daemon == null) {
            synchronized (Daemon.class) {
                if (daemon == null) {
                    daemon = new Daemon();
                }
            }
        }
        return daemon;
    }

    public void onCallback(String event) {
        String[] info = event.split("=");
        String type = info[0];
        String msg = info[1];
        //        Log.i("onCallback", "==================" + type + "   ============================
        //    " + msg);
        if (PyConstant.CALLBACK_STATUS.equals(type)) {
            if (!TextUtils.isEmpty(msg) && msg.length() != 2) {
                EventBus.getDefault().post(new SecondEvent(msg));
                EventBus.getDefault().post(new FirstEvent("22"));
            }
        } else if (PyConstant.CALLBACK_HISTORY.equals(event)) {
            // todo:
        } else if (PyConstant.CALLBACK_SERVER_STATUS.equals(event)) {
            // todo:
            //            Log.i("onCallback", "自定义节点添加+++++++++++++++++++++++    " + msg);
        }
    }
}
