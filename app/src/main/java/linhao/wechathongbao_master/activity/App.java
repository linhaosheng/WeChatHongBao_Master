package linhao.wechathongbao_master.activity;

import android.app.Application;

import com.antfortune.freeline.FreelineCore;

/**
 * Created by linhao on 2017/1/12.
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FreelineCore.init(this);
    }
}
