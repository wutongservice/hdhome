package com.borqs.se.weather;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import com.borqs.freehdhome.R;
import com.borqs.se.home3d.SEHomeUtils;
import com.borqs.se.weather.LoopForInfoService.LocalBinder;
import com.borqs.se.weather.yahoo.WeatherDataModel;
import com.borqs.se.weather.yahoo.WeatherPreferences;
import com.borqs.se.weather.yahoo.YahooLocationHelper.CityEntity;

public class WeatherSettings extends PreferenceActivity implements OnPreferenceClickListener,
        OnPreferenceChangeListener {

    public static final String PREFS_AUTO_UPDATE_SETTING = "auto_update_settings";
    private static final String KEY_AUTO_LOCATION = "auto_location";
    private static final String KEY_SET_LOCATION = "set_location";
    public static final String KEY_AUTO_UPDATE_SETTING = "auto_update_setting";
    public static final String KEY_AUTO_UPDATE = "auto_update";
    public static final String KEY_USE_CELSIUS = "use_celsius";
    private static final String KEY_WEATHER_UPDATE = "update_now";

    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_INTERVAL_TIME = "interval_time";

    public static final String TIME_START_HOUR_OF_DAY = "start_hour";
    public static final String TIME_START_MINUTE = "start_minute";
    public static final String TIME_REAL_START_HOUR_OF_DAY = "real_start_hour";
    public static final String TIME_REAL_START_MINUTE = "real_start_minute";
    public static final String TIME_END_HOUR_OF_DAY = "end_hour";
    public static final String TIME_END_MINUTE = "end_minute";

    public static final int DIALOG_START_TIME_PICKER = 0;
    public static final int DIALOG_END_TIME_PICKER = 1;
    public static final int PROGRESS_DIALOG_UPDATE_WEATHER = 2;

    public static final int MSG_SHOW_CITYS = 0;
    public static final int MSG_GET_CITYS = 1;

    private Preference mUpdateNow;
    private ProgressDialog mUpdateWeatherPD;
    private CheckBoxPreference mAutoLocation;
    private EditTextPreference mSetLocation;
    private CheckBoxPreference mAutoUpdatePreference;
    private Preference mStartTimePreference;
    private Preference mEndTimePreference;
    private ListPreference mIntervalPreference;
    private OnTimeSetListener mStartTimeSetListener;
    private OnTimeSetListener mEndTimeSetListener;

    private int mStartHour;
    private int mStartMinute;
    private int mEndHour;
    private int mEndMinute;
    LoopForInfoService mService;
    boolean mBound = false;

    private boolean mIsFinished = false;
    private Object mLock = new Object();

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_GET_CITYS:
                final String data = (String) msg.obj;

                new Thread() {
                    public void run() {
                        List<CityEntity> citys = WeatherDataModel.getInstance().getCityList(data);
                        synchronized (mLock) {
                            if (!mIsFinished) {
                                Message message = Message.obtain(mHandler, MSG_SHOW_CITYS, citys);
                                message.sendToTarget();
                            }
                        }
                    }
                }.start();
                break;
            case MSG_SHOW_CITYS:
                removeDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
                final List<CityEntity> citys = (List<CityEntity>) msg.obj;
                if (citys != null && citys.size() > 0) {
                    if (citys.size() > 1) {
                        String[] items = new String[citys.size()];
                        CityEntity c = null;
                        StringBuffer buffer = null;
                        for (int i = 0; i < items.length; i++) {
                            c = citys.get(i);
                            buffer = new StringBuffer();
                            if (!TextUtils.isEmpty(c.mCity)) {
                                buffer.append(c.mCity).append(",");
                            }
                            if (!TextUtils.isEmpty(c.mProvince)) {
                                buffer.append(c.mProvince).append(",");
                            }
                            if (!TextUtils.isEmpty(c.mCountry)) {
                                buffer.append(c.mCountry);
                            }
                            items[i] = buffer.toString();
                        }
                        AlertDialog dialog = new AlertDialog.Builder(WeatherSettings.this).setTitle(R.string.dialog_choose_city)
                                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        CityEntity city = citys.get(item);
                                        if (city != null) {
                                            Bundle bundle = new Bundle();
                                            bundle.putString(State.BUNDLE_COUNTRY, city.mCountry);
                                            bundle.putString(State.BUNDLE_PROVINCE, city.mProvince);
                                            bundle.putString(State.BUNDLE_CITY, city.mCity);
                                            bundle.putString(State.BUNDLE_WOEID, city.mWoeid);
                                            mService.requestWeatherByWoeid(bundle);
                                            citys.clear();
                                            dialog.dismiss();
                                        }
                                    }
                                }).show();
                    } else {
                        CityEntity city = citys.get(0);
                        if (city != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString(State.BUNDLE_COUNTRY, city.mCountry);
                            bundle.putString(State.BUNDLE_PROVINCE, city.mProvince);
                            bundle.putString(State.BUNDLE_CITY, city.mCity);
                            bundle.putString(State.BUNDLE_WOEID, city.mWoeid);
                            mService.requestWeatherByWoeid(bundle);
                            citys.clear();
                        } else {
                            Toast.makeText(WeatherSettings.this, getString(R.string.req_error), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                } else {
                    Toast.makeText(WeatherSettings.this, getString(R.string.req_error), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.weather_settings);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        bindService();
        synchronized (mLock) {
            mIsFinished = false;
        }
        int[] sTime = getTime(this, true);
        mStartHour = sTime[0];
        mStartMinute = sTime[1];

        int[] eTime = getTime(this, false);
        mEndHour = eTime[0];
        mEndMinute = eTime[1];

        initPreference();
        mStartTimeSetListener = new OnTimeSetListener() {

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay > mEndHour) {
                    Toast.makeText(WeatherSettings.this, "start time can not be earlyer than end time",
                            Toast.LENGTH_SHORT).show();
                } else if (hourOfDay == mEndHour && minute > mEndMinute) {
                    Toast.makeText(WeatherSettings.this, "start time can not be earlyer than end time",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mStartHour = hourOfDay;
                    mStartMinute = minute;
                    saveStartTime(hourOfDay, minute);
                }
                updateTimeDisplay();
            }
        };

        mEndTimeSetListener = new OnTimeSetListener() {

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay < mStartHour) {
                    Toast.makeText(WeatherSettings.this, "start time can not be earlyer than end time",
                            Toast.LENGTH_SHORT).show();
                } else if (hourOfDay == mStartHour && minute < mStartMinute) {
                    Toast.makeText(WeatherSettings.this, "start time can not be earlyer than end time",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mEndHour = hourOfDay;
                    mEndMinute = minute;
                    saveEndTime(hourOfDay, minute);
                }
                updateTimeDisplay();
            }
        };
        registerReceiver();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        removeDialog(DIALOG_START_TIME_PICKER);
        removeDialog(DIALOG_END_TIME_PICKER);
        unbindService(mConnection);
        synchronized (mLock) {
            mIsFinished = true;
            mHandler.removeMessages(MSG_SHOW_CITYS);
        }
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        removeDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_INTERVAL_TIME.equals(preference.getKey())) {
            long interval = Long.parseLong((String) newValue);
            saveIntervalValue(interval);
            updateIntervalDisplay();
        } else if (KEY_AUTO_LOCATION.equals(preference.getKey())) {
            boolean state = (Boolean) newValue;
            SharedPreferences settings = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(KEY_AUTO_LOCATION, state);
            editor.commit();
            if (state) {
                showDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
                mService.saveUpdateTime(-1);
                mService.requestWeather();
            } else {
                String city = WeatherPreferences.getInstance(this).getManualLocationCityName();
                if (!TextUtils.isEmpty(city)) {
                    showDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
                    mService.saveUpdateTime(-1);
                    mService.requestWeather();
                } 
            }
        } else if (KEY_SET_LOCATION.equals(preference.getKey())) {
            String city = ((String) newValue).toLowerCase().trim().replaceAll(" ", "_");
            if (TextUtils.isEmpty(city)) {
                Toast.makeText(WeatherSettings.this, R.string.city_name_error, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (mUpdateWeatherPD == null || !mUpdateWeatherPD.isShowing()) {
                showDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
            }
            mService.saveUpdateTime(-1);
            Message msg = Message.obtain(mHandler, MSG_GET_CITYS, city);
            mHandler.sendMessage(msg);
        } else if (KEY_AUTO_UPDATE.equals(preference.getKey())) {
            boolean state = (Boolean) newValue;
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_AUTO_UPDATE, state);
            editor.commit();
            if (state) {
                mService.setAlarm(true);
            } else {
                mService.removeAlarm();
            }
        } else if (KEY_USE_CELSIUS.equals(preference.getKey())) {
            boolean state = (Boolean) newValue;
            SharedPreferences settings = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(KEY_USE_CELSIUS, state);
            editor.commit();
            mService.forceRefresh();
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (KEY_START_TIME.equals(preference.getKey())) {
            removeDialog(DIALOG_START_TIME_PICKER);
            removeDialog(DIALOG_END_TIME_PICKER);
            showDialog(DIALOG_START_TIME_PICKER);
        } else if (KEY_SET_LOCATION.equals(preference.getKey())) {
            mSetLocation.getEditText().setText("");
        } else if (KEY_END_TIME.equals(preference.getKey())) {
            removeDialog(DIALOG_START_TIME_PICKER);
            removeDialog(DIALOG_END_TIME_PICKER);
            showDialog(DIALOG_END_TIME_PICKER);
        } else if (KEY_WEATHER_UPDATE.equals(preference.getKey())) {
            if (!Utils.checkNetworkIsAvailable(this)) {
                Toast.makeText(WeatherSettings.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            } else if (isAutoLocation(this)) {
                showDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
                mService.saveUpdateTime(-1);
                mService.requestWeather();
            } else {
                String city = WeatherPreferences.getInstance(this).getManualLocationCityName();
                if (!TextUtils.isEmpty(city)) {
                    showDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
                    mService.saveUpdateTime(-1);
                    mService.requestWeather();
                } else {
                    Toast.makeText(WeatherSettings.this, R.string.city_name_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case PROGRESS_DIALOG_UPDATE_WEATHER:
            if (mUpdateWeatherPD == null) {
                mUpdateWeatherPD = new ProgressDialog(this);
                mUpdateWeatherPD.setTitle(R.string.wait_dialog_title);
                mUpdateWeatherPD.setMessage(getString(R.string.wait_dialog_msg_weather));
            }
            if (!mUpdateWeatherPD.isShowing()) {
                return mUpdateWeatherPD;
            }
            break;
        case DIALOG_START_TIME_PICKER:
            dialog = new TimePickerDialog(this, mStartTimeSetListener, mStartHour, mStartMinute, true);
            break;
        case DIALOG_END_TIME_PICKER:
            dialog = new TimePickerDialog(this, mEndTimeSetListener, mEndHour, mEndMinute, true);
            break;
        default:
            break;
        }
        return dialog;
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.weather_settings);
        mUpdateNow = findPreference(KEY_WEATHER_UPDATE);
        String time = WeatherPreferences.getInstance(this).getUpdateTimeString();
        if (time != null) {
            mUpdateNow.setSummary(time);
        }
        mUpdateNow.setOnPreferenceClickListener(this);
        mAutoLocation = (CheckBoxPreference) findPreference(KEY_AUTO_LOCATION);
        mAutoLocation.setChecked(isAutoLocation(this));
        mAutoLocation.setOnPreferenceChangeListener(this);
        mSetLocation = (EditTextPreference) findPreference(KEY_SET_LOCATION);
        mSetLocation.setOnPreferenceClickListener(this);
        mSetLocation.setOnPreferenceChangeListener(this);
        mSetLocation.setSummary(WeatherPreferences.getInstance(this).getManualLocationCityName());

        mAutoUpdatePreference = (CheckBoxPreference) findPreference(KEY_AUTO_UPDATE);
        mStartTimePreference = findPreference(KEY_START_TIME);
        mEndTimePreference = findPreference(KEY_END_TIME);
        mIntervalPreference = (ListPreference) findPreference(KEY_INTERVAL_TIME);

        mAutoUpdatePreference.setChecked(isAutoUpdate(this));
        mAutoUpdatePreference.setOnPreferenceChangeListener(this);

        mStartTimePreference.setOnPreferenceClickListener(this);
        mEndTimePreference.setOnPreferenceClickListener(this);

        mIntervalPreference.setOnPreferenceChangeListener(this);
        mIntervalPreference.setValue(String.valueOf(getCurrentIntervalValue(this)));

        CheckBoxPreference tempUnits = (CheckBoxPreference) findPreference(KEY_USE_CELSIUS);
        tempUnits.setChecked(isCelsius(this));
        tempUnits.setOnPreferenceChangeListener(this);

        updateTimeDisplay();
        updateIntervalDisplay();
    }

    private void updateTimeDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, mStartHour);
        cal.set(Calendar.MINUTE, mStartMinute);

        Date start = cal.getTime();
        mStartTimePreference.setSummary(DateFormat.getTimeFormat(this).format(start));

        cal.set(Calendar.HOUR_OF_DAY, mEndHour);
        cal.set(Calendar.MINUTE, mEndMinute);

        Date end = cal.getTime();
        mEndTimePreference.setSummary(DateFormat.getTimeFormat(this).format(end));
    }

    private void updateIntervalDisplay() {
        int index = 3;
        String[] values = getResources().getStringArray(R.array.interval_time_values);
        String[] entries = getResources().getStringArray(R.array.interval_time_entries);
        String intervalString = String.valueOf(getCurrentIntervalValue(this));
        for (int i = 0; i < values.length; i++) {
            if (intervalString.equals(values[i])) {
                index = i;
                break;
            }
        }
        if (index < entries.length) {
            mIntervalPreference.setSummary(entries[index]);
        }
    }

    public static long getCurrentIntervalValue(Context context) {
        long defaultValue = 1000 * 60 * 60 * 2/* 180000 */;
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        return settings.getLong(KEY_INTERVAL_TIME, defaultValue);
    }

    private void saveIntervalValue(long interval) {
        SharedPreferences settings = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(KEY_INTERVAL_TIME, interval);
        editor.commit();
        if (isAutoUpdate(this)) {
            mService.setAlarm(true);
        }
    }

    public static int[] getTime(Context context, boolean isStartTime) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        if (isStartTime) {
            int[] start = { settings.getInt(TIME_START_HOUR_OF_DAY, 7), settings.getInt(TIME_START_MINUTE, 0) };
            return start;
        } else {
            int[] end = { settings.getInt(TIME_END_HOUR_OF_DAY, 23), settings.getInt(TIME_END_MINUTE, 0) };
            return end;
        }
    }

    public static int[] getRealStartTime(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        int hour = settings.getInt(TIME_START_HOUR_OF_DAY, 7);
        int min = settings.getInt(TIME_START_MINUTE, 0);
        int[] start = { settings.getInt(TIME_REAL_START_HOUR_OF_DAY, hour),
                settings.getInt(TIME_REAL_START_MINUTE, min) };
        return start;
    }

    private void saveStartTime(int hour, int minute) {
        saveRealStartTime(this, hour, minute);
        SharedPreferences settings = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TIME_START_HOUR_OF_DAY, hour);
        editor.putInt(TIME_START_MINUTE, minute);
        editor.commit();
        if (isAutoUpdate(this)) {
            mService.setAlarm(true);
        }
    }

    public static void saveRealStartTime(Context context, int hour, int minute) {
        int[] realStartTime = getRealStartTime(hour, minute);
        SharedPreferences settings = context.getSharedPreferences(WeatherSettings.PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(WeatherSettings.TIME_REAL_START_HOUR_OF_DAY, realStartTime[0]);
        editor.putInt(WeatherSettings.TIME_REAL_START_MINUTE, realStartTime[1]);
        editor.commit();
    }

    private void saveEndTime(int hour, int minute) {
        SharedPreferences settings = getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TIME_END_HOUR_OF_DAY, hour);
        editor.putInt(TIME_END_MINUTE, minute);
        editor.commit();
        if (isAutoUpdate(this)) {
            mService.setAlarm(true);
        }
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, LoopForInfoService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WeatherController.INTENT_ACTION_WEATHER_UPDATE);
        registerReceiver(mIntentReceiver, filter);
    }

    public static boolean isAutoUpdate(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        return settings.getBoolean(KEY_AUTO_UPDATE, true);
    }

    public static void saveLastAlarmTime(Context context, long last) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("last_alarm", last);
        editor.commit();
    }

    public static boolean isAutoLocation(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        return settings.getBoolean(KEY_AUTO_LOCATION, true);
    }

    public static void setAutoLocation(Context context, boolean isTrue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_AUTO_LOCATION, isTrue);
        editor.commit();
    }

    public static boolean isCelsius(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        return settings.getBoolean(KEY_USE_CELSIUS, true);
    }

    public static boolean getGLSPromptStatus(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        return settings.getBoolean("GLS", false);
    }

    public static void setGLSPromptStatus(Context context, boolean hasShow) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_AUTO_UPDATE_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("GLS", hasShow);
        editor.commit();
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WeatherController.INTENT_ACTION_WEATHER_UPDATE.equals(intent.getAction())) {
                if (intent.hasExtra(String.valueOf("state"))) {
                    int resultCode = intent.getIntExtra(String.valueOf("state"), -1);
                    int resId = -1;
                    int toastTime = Toast.LENGTH_LONG;
                    switch (resultCode) {
                    case WeatherController.RESULT_ERROR_GET_WEATHER:
                        resId = R.string.req_error;
                        break;
                    case WeatherController.RESULT_SUCCESS:
                        resId = R.string.weather_update_success;
                        toastTime = Toast.LENGTH_SHORT;
                        break;
                    case WeatherController.RESULT_ERROR_LOCATION:
                        resId = R.string.locate_error;
                        break;
                    case WeatherController.RESULT_ERROR_WEATHER_INFO:
                        resId = R.string.get_latest_weather_failed;
                        break;
                    default:
                        break;
                    }
                    if (resId != -1) {
                        Toast.makeText(WeatherSettings.this, resId, toastTime).show();
                    }
                }
                String city = WeatherPreferences.getInstance(WeatherSettings.this).getManualLocationCityName();
                mSetLocation.setSummary(city);
            }
            removeDialog(PROGRESS_DIALOG_UPDATE_WEATHER);
        }
    };

    public static int[] getRealStartTime(int hour, int min) {
        if (SEHomeUtils.DEBUG)
            Log.d("Weather", "start time: " + hour + ", " + min);
        int[] realStart = { hour, min };
        if (hour > 0) {
            int realHour = hour - 1;
            int realSecond = min + getRandom(0, 60, 1);
            if (realSecond >= 60) {
                realStart[0] = realHour + 1;
                realStart[1] = realSecond % 60;
            } else {
                realStart[0] = realHour;
                realStart[1] = realSecond;
            }
        } else if (min > 0) {
            realStart[0] = hour;
            realStart[1] = getRandom(0, min, 1);
        }
        Log.i("Weather", "real start time: " + realStart[0] + ", " + realStart[1]);
        return realStart;
    }

    public static int getRandom(int min, int max, int step) {
        int count = (max - min) / step;
        double x = Math.random() * count;
        return ((int) (x) * step + min);
    }
}
