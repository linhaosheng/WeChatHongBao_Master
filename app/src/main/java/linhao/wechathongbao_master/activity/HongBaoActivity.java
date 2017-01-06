package linhao.wechathongbao_master.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import linhao.wechathongbao_master.R;
import linhao.wechathongbao_master.modle.Money;
import linhao.wechathongbao_master.utils.Preferences;


/**
 * Created by linhao on 2017/1/2.
 */
public class HongBaoActivity extends AppCompatActivity {

    private List<Money> moneyList = null;
    private ListView monetView;
    private MoneyAdapter moneyAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hongbao_list);
        monetView = (ListView) findViewById(R.id.listview);
        Set<String> money = Preferences.getMoney(this);
        moneyList = new ArrayList<>();
        if (money != null) {
            for (String objValue : money) {
                Money moneyInfo = (Money) Preferences.getObject(objValue);
                moneyList.add(moneyInfo);
            }
        }
        moneyAdapter = new MoneyAdapter(this);
        monetView.setAdapter(moneyAdapter);
    }

    public class MoneyAdapter extends BaseAdapter {

        private Context context;

        private MoneyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return moneyList == null ? 0 : moneyList.size();
        }

        @Override
        public Object getItem(int position) {
            Money money = moneyList.get(position);
            if (money != null) {
                return money;
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MoneyViewHold moneyViewHold = null;
            if (convertView == null) {
                View view = LayoutInflater.from(context).inflate(R.layout.money_item, parent, false);
                moneyViewHold = new MoneyViewHold();
                moneyViewHold.time = (TextView) view.findViewById(R.id.time);
                moneyViewHold.resource = (TextView) view.findViewById(R.id.resource);
                moneyViewHold.money = (TextView) view.findViewById(R.id.momney);
                view.setTag(moneyViewHold);
            } else {
                moneyViewHold = (MoneyViewHold) convertView.getTag();
            }
            Money money = moneyList.get(position);
            moneyViewHold.time.setText(money.getTime());
            moneyViewHold.resource.setText(money.getResource());
            moneyViewHold.money.setText(money.getMoney());
            return convertView;
        }
    }

    private class MoneyViewHold {
        private TextView time;
        private TextView resource;
        private TextView money;
    }
}

