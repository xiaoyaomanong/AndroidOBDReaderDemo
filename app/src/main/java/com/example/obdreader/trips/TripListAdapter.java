package com.example.obdreader.trips;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.obdreader.R;

import java.util.Date;

public class TripListAdapter extends ArrayAdapter<TripRecord> {
    private final Activity activity;
    private final java.util.List<TripRecord> records;//要显示的行程记录列表
    public TripListAdapter(Activity activity, java.util.List<TripRecord> records) {
        super(activity, R.layout.row_trip_list, records);
        this.activity = activity;
        this.records = records;
    }

    /**
     * 描述：
     * 构造并填充一个视图，以在索引处显示TripRecord由position参数指定的列表。
     *
     * @see ArrayAdapter#getView(int, android.view.View, ViewGroup)
     */
    @SuppressLint("SetTextI18n")
    @Override
    public android.view.View getView(int position, android.view.View view, ViewGroup parent) {
        // 为该行创建一个视图（如果尚不存在）
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(R.layout.row_trip_list, null);
        }

        // 从视图中获取小部件
      TextView startDate =  view.findViewById(R.id.startDate);
      TextView columnDuration =  view.findViewById(R.id.columnDuration);
      TextView rowEngine =  view.findViewById(R.id.rowEngine);
      TextView rowOther =  view.findViewById(R.id.rowOther);
        // 从记录数据填充行小部件
        TripRecord record = records.get(position);
        startDate.setText(record.getStartDateString());
        columnDuration.setText(calcDiffTime(record.getStartDate(), record.getEndDate()));
        String rpmMax = String.valueOf(record.getEngineRpmMax());
        String engineRuntime = record.getEngineRuntime();
        if (engineRuntime == null)
            engineRuntime = "None";
        rowEngine.setText("引擎运转时间: " + engineRuntime + "\t最高转速: " + rpmMax);
        rowOther.setText("最大速度: " + String.valueOf(record.getSpeedMax()));
        return view;
    }

    private String calcDiffTime(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuffer res = new StringBuffer();

        if (diffDays > 0)
            res.append(diffDays + "d");

        if (diffHours > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffHours + "h");
        }

        if (diffMinutes > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffMinutes + "m");
        }

        if (diffSeconds > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }

            res.append(diffSeconds + "s");
        }
        return res.toString();
    }

    /**
     * 描述：
     * 当基础数据集更改时，由父级调用。
     *
     * @see ArrayAdapter#notifyDataSetChanged()
     */
    @Override
    public void notifyDataSetChanged() {
        //配置可能已更改-获取当前设置
        //todo
        //getSettings();
        super.notifyDataSetChanged();
    }
}
