package com.sollian.base;

/**
 * @author admin on 2018/4/12.
 */

public class Demo {
    public void move() {
        Util util = new Util();
        util.run();
        Util.show();
        util.name = "hah";
        int aa = Util.AGE;

        SubUtil subUtil = new SubUtil();
        subUtil.run2();
        SubUtil.show2();
        subUtil.name2 = "hah";
        int aaa = SubUtil.AGE2;
    }
}
