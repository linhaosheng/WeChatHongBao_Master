package linhao.wechathongbao_master.modle;

import java.io.Serializable;

/**
 * Created by linhao on 2017/1/2.
 */
public class Money implements Serializable {
    //抢到的红包的时间
    private String time;
    //抢到红包的金额
    private String money;
    //红包的来源
    private String resource;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
