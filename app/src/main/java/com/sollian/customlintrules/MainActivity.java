package com.sollian.customlintrules;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sollian.base.SubUtil;
import com.sollian.base.Util;

public class MainActivity extends Activity {

    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("---", "hah");
        new Util().run();
        new Util();
        Util util = new Util();
        util.run();
        Util.show();
        util.name = "hah";
        int aa = Util.AGE;

        SubUtil subUtil = new SubUtil();
        subUtil.run2();
        SubUtil.show2();
        subUtil.name2 = "hah";
        subUtil.name = "hah";
        int aaa = SubUtil.AGE2;

        Object obj = subUtil;
        SubUtil su = (SubUtil) obj;

        long b = 0;
        b = 1;

        Class<?> clazz = Util.class;

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).run();

        init();
    }

    public void init() {

    }
}