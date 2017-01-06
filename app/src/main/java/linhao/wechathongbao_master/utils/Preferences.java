package linhao.wechathongbao_master.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import linhao.wechathongbao_master.accessibility.HongBaoService;

/**
 * Created by linhao on 2017/1/2.
 */
public class Preferences {

    /**
     * 保存红包的数据
     *
     * @param context
     */
    public static void saveMoney(Context context) {
        SharedPreferences money = context.getSharedPreferences("money", Context.MODE_PRIVATE);
        money.edit().putStringSet("moneyInfo", HongBaoService.moneys).apply();
    }

    /**
     * 获取红包列表
     * @param context
     * @return
     */
    public static Set<String> getMoney(Context context) {
        SharedPreferences money = context.getSharedPreferences("money", Context.MODE_PRIVATE);
        Set<String> moneyInfo = money.getStringSet("moneyInfo", null);
        if (moneyInfo != null) {
            return moneyInfo;
        }
        return null;
    }

    /**
     * 把Obj对象进行压缩转变为字符串
     *
     * @param obj
     * @return
     */
    public static String setObject(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            String objValue = new String(Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT));
            return objValue;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static <T> T getObject(String objValue) {
        byte[] decode = Base64.decode(objValue, Base64.DEFAULT);
        ByteArrayInputStream bis = new ByteArrayInputStream(decode);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            try {
                T t = (T) ois.readObject();
                return t;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
