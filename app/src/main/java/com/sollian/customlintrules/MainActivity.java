package com.sollian.customlintrules;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sollian.base.SubUtil;
import com.sollian.base.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Day);
        setContentView(R.layout.activity_main);

        int arr[] = new int[3];

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
        List<Object> objects = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Integer) {
                objects.remove(object);
            }
        }

        for (Object object : objects) {
            if (object instanceof Integer) {
                objects.remove(object);
                break;
            }
        }

        for (Object object : objects) {
            if (object instanceof Integer) {
                objects.remove(object);
                return;
            }
        }
    }
}
