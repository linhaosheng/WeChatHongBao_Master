package linhao.wechathongbao_master.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import linhao.wechathongbao_master.modle.Money;
import linhao.wechathongbao_master.utils.PerformClickUtils;
import linhao.wechathongbao_master.utils.Preferences;


/**
 * Created by linhao on 2017/1/2.
 */
public class HongBaoService extends AccessibilityService {

    //谁的红包
    private static String hongbao_resource = "com.tencent.mm:id/bai";
    //红包的金额
    private static String hongbao_number = "com.tencent.mm:id/bam";
    //红包已经过期
    private static String hongbao_expire = "com.tencent.mm:id/bdg";
    //关闭过期红包的界面
    private static String hongbao_expire_close = "com.tencent.mm:id/bdl";
    //开红包的ID
    private static String hongbao_open = "com.tencent.mm:id/bdh";

    private String resource = "";
    private String number = "";
    private SimpleDateFormat dateFormat = null;
    private Date date = null;
    public static Set<String> moneys = null;
    private static boolean start;
    private ScreenBroadcastReceiver receiver;
    private static boolean isScreenBlock;
    //声明键盘管理器
    private KeyguardManager keyguardManager;
    // 声明键盘锁
    private KeyguardManager.KeyguardLock keyguardLock;
    //声明电源管理器
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        dateFormat = new SimpleDateFormat("yyyy-mm-dd:HH:mm:ss");
        moneys = new HashSet<>();
        start = true;
        Toast.makeText(this, "开始抢红包", Toast.LENGTH_SHORT).show();
        System.out.println("connect--------");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate--------");
        receiver = new ScreenBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        this.registerReceiver(receiver, intentFilter);
        keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Tag");
        //初始化键盘锁，可以锁定或解开键盘锁
        keyguardLock = keyguardManager.newKeyguardLock("");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        System.out.println("enentType----" + String.valueOf(eventType));
        if (!start){
            return;
        }
        switch (eventType) {
            //获取通知栏事件
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (isScreenBlock) {
                    wakeLock();
                }
                List<CharSequence> text = event.getText();
                if (!text.isEmpty()) {
                    for (CharSequence sequence : text) {
                        String message = String.valueOf(sequence);
                        if (message.contains("微信红包")) {
                            openNotify(event);
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                openHongBao(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                findHongBao(event);
                break;
        }
    }

    /**
     * 因为 AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED 获取到的 className 是 窗口的布局 的 className
     * 所以只要只要窗口内容出现领取红包就点击
     *
     * @param event
     */
    private void findHongBao(AccessibilityEvent event) {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
        List<AccessibilityNodeInfo> listPacket = rootInActiveWindow.findAccessibilityNodeInfosByText("领取红包");
        if (listPacket != null && !listPacket.isEmpty()) {
            //点击最新的一个红包
            AccessibilityNodeInfo parent = listPacket.get(listPacket.size() - 1).getParent();
            if (parent != null) {
                PerformClickUtils.performClick(parent);
            }
        }
    }
    @Override
    public void onInterrupt() {
        start = false;
        releaseLock();
        Toast.makeText(this, "_抢红包服务被中断啦_", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseLock();
    }
    /**
     * 点击通知栏的消息
     *
     * @param event
     */
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent contentIntent = notification.contentIntent;
        try {
            contentIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 进入抢红包界面
     *
     * @param event
     */
    private void openHongBao(AccessibilityEvent event) {
        System.out.println("evenClass-----" + event.getClassName());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            // 拆红包界面
            getPacket(this);;
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            // 拆完红包后，看红包金额的界面
            getMoney();
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            // 聊天界面
            openPacket(event);
        }
    }

    /**
     * 在聊天界面查找红包，并点击
     */
    private void openPacket(AccessibilityEvent event) {
        System.out.println("openPacket----");
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
        List<AccessibilityNodeInfo> listPacket = rootInActiveWindow.findAccessibilityNodeInfosByText("领取红包");
        if (listPacket != null) {
            for (int i = listPacket.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = listPacket.get(i).getParent();
                if (parent != null) {
                    PerformClickUtils.performClick(parent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!"com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
                        PerformClickUtils.performBack(this);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 拆红包
     */
    private void getPacket(AccessibilityService accessibilityService) {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
        //若出现 “该红包已超过24小时。如已领取，可在“我的红包”中查看" 则直接返回
        List<AccessibilityNodeInfo> back = rootInActiveWindow.findAccessibilityNodeInfosByViewId(hongbao_expire);
        if (back != null) {
            List<AccessibilityNodeInfo> nodeInfoId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(hongbao_expire_close);  //关闭ID
            if (nodeInfoId != null && !nodeInfoId.isEmpty()) {
                PerformClickUtils.findViewIdAndClick(this, hongbao_expire_close);
            } else {
                PerformClickUtils.performBack(this);
            }
            PerformClickUtils.findTextAndClick(this, "聊天信息");
        }
        List<AccessibilityNodeInfo> hongbao = rootInActiveWindow.findAccessibilityNodeInfosByText("拆红包");
        if (hongbao != null && !hongbao.isEmpty()) {
            PerformClickUtils.findTextAndClick(accessibilityService, "拆红包");
        } else {
            List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(hongbao_open);//开红包的ID
            if (accessibilityNodeInfosByViewId != null && !accessibilityNodeInfosByViewId.isEmpty()) {
                PerformClickUtils.findViewIdAndClick(this, hongbao_open);
            }
        }
    }

    /**
     * 获取红包的金额和来源
     */
    private void getMoney() {
        //获取当前时间
        date = new Date(System.currentTimeMillis());
        String time = dateFormat.format(date);
        Money money = new Money();
        money.setTime(time);
        System.out.println("time--" + time);
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
        //获取钱包来源
        List<AccessibilityNodeInfo> accessibilityNodeInfosResource = rootInActiveWindow.findAccessibilityNodeInfosByViewId(hongbao_resource);
        if (accessibilityNodeInfosResource != null && accessibilityNodeInfosResource.size() > 0) {
            if (accessibilityNodeInfosResource.get(0) != null) {
                resource = accessibilityNodeInfosResource.get(0).getText().toString();
                money.setResource(resource);
            }
        }
        //获取钱包金额
        List<AccessibilityNodeInfo> accessibilityNodeInfosNumber = rootInActiveWindow.findAccessibilityNodeInfosByViewId(hongbao_number);
        if (accessibilityNodeInfosNumber != null && !accessibilityNodeInfosNumber.isEmpty()) {
            if (accessibilityNodeInfosNumber.get(0) != null) {
                number = accessibilityNodeInfosNumber.get(0).getText().toString();
                money.setMoney(number);
            }
        }
        System.out.println("money----" + money.getMoney());
        String objValue = Preferences.setObject(money);
        moneys.add(objValue);
        //点击返回按钮
        List<AccessibilityNodeInfo> back = rootInActiveWindow.findAccessibilityNodeInfosByText("返回");
        if (back != null) {
            PerformClickUtils.findTextAndClick(this, "返回");
        } else {
            PerformClickUtils.performBack(this);
        }
        PerformClickUtils.findTextAndClick(this, "聊天信息");
    }
    /**
     * 解锁屏幕
     */
    public void wakeLock() {
        wakeLock.acquire();
        //禁用显示键盘锁定
        keyguardLock.disableKeyguard();
    }
    /**
     * 解除键盘锁
     */
    private void releaseLock() {
   //     wakeLock.release();
    }

    public class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    isScreenBlock = false;
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    isScreenBlock = true;
                    break;
                case Intent.ACTION_USER_PRESENT:
                    isScreenBlock = true;
                    break;
            }
        }
    }
}
