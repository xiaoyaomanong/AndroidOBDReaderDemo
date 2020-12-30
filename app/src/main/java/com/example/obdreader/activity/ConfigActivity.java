package com.example.obdreader.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.example.obdreader.R;
import com.example.obdreader.config.ObdConfig;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.util.ArrayList;

/**
 * Configuration com.github.pires.obd.reader.activity.
 */
public class ConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String UPLOAD_URL_KEY = "upload_url_preference";
    public static final String UPLOAD_DATA_KEY = "upload_data_preference";
    public static final String OBD_UPDATE_PERIOD_KEY = "obd_update_period_preference";
    public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
    public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
    public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
    public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
    public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
    public static final String PROTOCOLS_LIST_KEY = "obd_protocols_preference";
    public static final String ENABLE_GPS_KEY = "enable_gps_preference";
    public static final String GPS_UPDATE_PERIOD_KEY = "gps_update_period_preference";
    public static final String GPS_DISTANCE_PERIOD_KEY = "gps_distance_period_preference";
    public static final String ENABLE_BT_KEY = "enable_bluetooth_preference";
    public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
    public static final String CONFIG_READER_KEY = "reader_config_preference";
    public static final String ENABLE_FULL_LOGGING_KEY = "enable_full_logging";
    public static final String DIRECTORY_FULL_LOGGING_KEY = "dirname_full_logging";
    public static final String DEV_EMAIL_KEY = "dev_email";

    /**
     * @param prefs
     * @return
     */
    public static int getObdUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.
                getString(ConfigActivity.OBD_UPDATE_PERIOD_KEY, "1"); // 1秒
        int period = 4000; //默认为4000ms
        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 4000;
        }

        return period;
    }

    /**
     * @param prefs
     * @return 获得体积效率
     */
    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    /**
     * @param prefs
     * @return 获得发动机排量
     */
    public static double getEngineDisplacement(SharedPreferences prefs) {
        String edString = prefs.getString(ConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
        double ed = 1.6;
        try {
            ed = Double.parseDouble(edString);
        } catch (Exception e) {
        }
        return ed;
    }

    /**
     * @param prefs
     * @return 获取Obd命令
     */
    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        ArrayList<ObdCommand> ucmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            ObdCommand cmd = cmds.get(i);
            boolean selected = prefs.getBoolean(cmd.getName(), true);
            if (selected)
                ucmds.add(cmd);
        }
        return ucmds;
    }

    /**
     * @param prefs
     * @return 获得最大燃油经济性
     */
    public static double getMaxFuelEconomy(SharedPreferences prefs) {
        String maxStr = prefs.getString(ConfigActivity.MAX_FUEL_ECON_KEY, "70");
        double max = 70;
        try {
            max = Double.parseDouble(maxStr);
        } catch (Exception e) {
        }
        return max;
    }


    /**
     * 位置更新之间的最短时间（以毫秒为单位）
     *
     * @param prefs
     * @return
     */
    public static int getGpsUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_UPDATE_PERIOD_KEY, "1"); // 1 as in seconds
        int period = 1000; // by default 1000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 1000;
        }

        return period;
    }

    /**
     * 位置更新之间的最小距离，以米为单位
     *
     * @param prefs
     * @return
     */
    public static float getGpsDistanceUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_DISTANCE_PERIOD_KEY, "5"); // 5 as in meters
        float period = 5; // by default 5 meters

        try {
            period = Float.parseFloat(periodString);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 5;
        }

        return period;
    }

    /*
     * 读取首选项资源可在res / xml / preferences.xml上获得
     */
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        checkGps();
        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        ArrayList<CharSequence> vals = new ArrayList<>();
        ListPreference listBtDevices = (ListPreference) getPreferenceScreen()
                .findPreference(BLUETOOTH_LIST_KEY);
        ArrayList<CharSequence> protocolStrings = new ArrayList<>();
        ListPreference listProtocols = (ListPreference) getPreferenceScreen()
                .findPreference(PROTOCOLS_LIST_KEY);
        String[] prefKeys = new String[]{ENGINE_DISPLACEMENT_KEY,
                VOLUMETRIC_EFFICIENCY_KEY, OBD_UPDATE_PERIOD_KEY, MAX_FUEL_ECON_KEY};
        for (String prefKey : prefKeys) {
            EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
                    .findPreference(prefKey);
            txtPref.setOnPreferenceChangeListener(this);
        }

        /*
         * 可用的OBD命令
         *
         * TODO 这应该从首选项数据库中读取
         */
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen().findPreference(COMMANDS_SCREEN_KEY);
        for (ObdCommand cmd : cmds) {
            CheckBoxPreference cpref = new CheckBoxPreference(this);
            cpref.setTitle(cmd.getName());
            cpref.setKey(cmd.getName());
            cpref.setChecked(true);
            cmdScr.addPreference(cpref);
        }

        /*
         *可用的OBD协议
         *
         */
        for (ObdProtocols protocol : ObdProtocols.values()) {
            protocolStrings.add(protocol.name());
        }
        listProtocols.setEntries(protocolStrings.toArray(new CharSequence[0]));
        listProtocols.setEntryValues(protocolStrings.toArray(new CharSequence[0]));

        /*
         * 让我们使用此设备的蓝牙适配器选择配对的OBD-II
         * 我们将使用的兼容设备。
         */
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            listBtDevices
                    .setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

            // 我们不应该到这里，仍然警告用户
            Toast.makeText(this, "此设备不支持蓝牙。",
                    Toast.LENGTH_LONG).show();

            return;
        }

        /*
         * 收听首选项单击。
         *
         * TODO 有很多重复的验证:-/
         */
        final Activity thisActivity = this;
        listBtDevices.setEntries(new CharSequence[1]);
        listBtDevices.setEntryValues(new CharSequence[1]);
        listBtDevices.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(android.preference.Preference preference) {
                // 明白我在上一条评论中的意思吗？
                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                    Toast.makeText(thisActivity,
                            "该设备不支持蓝牙或已禁用。",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        /*
         * 获取已配对的设备并填充首选项列表。
         */
        java.util.Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
    }

    /**
     * OnPreferenceChangeListener方法将验证首选项
     * 更改时的值。
     *
     * @param preference the changed preference
     * @param newValue   the value to be validated and set if valid
     */
    public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {

        if (OBD_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
                || ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
                || MAX_FUEL_ECON_KEY.equals(preference.getKey())
                || GPS_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || GPS_DISTANCE_PERIOD_KEY.equals(preference.getKey())) {
            try {
                Double.parseDouble(newValue.toString().replace(",", "."));
                return true;
            } catch (Exception e) {
                Toast.makeText(this,
                        "Couldn't parse '" + newValue.toString() + "' as a number.",
                        Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    private void checkGps() {
        LocationManager mLocService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocService != null) {
            LocationProvider mLocProvider = mLocService.getProvider(LocationManager.GPS_PROVIDER);
            if (mLocProvider == null) {
                hideGPSCategory();
            }
        }
    }

    private void hideGPSCategory() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getResources().getString(R.string.pref_gps_category));
        if (preferenceCategory != null) {
            preferenceCategory.removeAll();
            preferenceScreen.removePreference((android.preference.Preference) preferenceCategory);
        }
    }
}
