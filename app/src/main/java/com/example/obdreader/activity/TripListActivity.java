package com.example.obdreader.activity;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.obdreader.R;
import com.example.obdreader.trips.TripListAdapter;
import com.example.obdreader.trips.TripLog;
import com.example.obdreader.trips.TripRecord;

import roboguice.activity.RoboActivity;

import static com.example.obdreader.activity.ConfirmDialog.createDialog;


/**
 * Some code taken from https://github.com/wdkapps/FillUp
 * 旅行清单
 */

public class TripListActivity
        extends RoboActivity
        implements ConfirmDialog.Listener {

    private java.util.List<TripRecord> records;
    private TripLog triplog = null;
    private TripListAdapter adapter = null;

    /// 从记录列表中当前选择的行
    private int selectedRow;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);

        android.widget.ListView lv = (android.widget.ListView) findViewById(R.id.tripList);

        triplog = TripLog.getInstance(this.getApplicationContext());
        records = triplog.readAllRecords();
        adapter = new TripListAdapter(this, records);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        // 创建菜单
        getMenuInflater().inflate(R.menu.context_trip_list, menu);

        //获取当前选中行的索引
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedRow = (int) info.id;

        // 获取当前选择的记录
        TripRecord record = records.get(selectedRow);
    }

    private void deleteTrip() {
        // 获取要从我们的记录列表中删除的记录
        TripRecord record = records.get(selectedRow);
        // 尝试从日志中删除记录
        if (triplog.deleteTrip(record.getID())) {

            //从我们的记录列表中删除记录
            records.remove(selectedRow);

            // 更新列表视图
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        // 获取当前选中行的索引
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selectedRow = (int) info.id;
        if (item.getItemId() == R.id.itemDelete) {
            showDialog(ConfirmDialog.DIALOG_CONFIRM_DELETE_ID);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    protected android.app.Dialog onCreateDialog(int id) {
        return createDialog(id, this, this);
    }

    /**
     * 描述：用户选择要从日志中删除的汽油记录并确认删除后调用。
     */
    protected void deleteRow() {
        // 获取要从我们的记录列表中删除的记录
        TripRecord record = records.get(selectedRow);
        // 尝试从日志中删除记录
        if (triplog.deleteTrip(record.getID())) {
            records.remove(selectedRow);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfirmationDialogResponse(int id, boolean confirmed) {
        removeDialog(id);
        if (!confirmed) return;

        if (id == ConfirmDialog.DIALOG_CONFIRM_DELETE_ID) {
            deleteRow();
        } else {
            Toast.makeText(this, "Invalid dialog id.", Toast.LENGTH_SHORT).show();
        }
    }
}
