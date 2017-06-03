package com.candykk.calllog;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int mYear, mMonth, mDay, mHour, mMinute, mCallType;
    Button btnDatePick;
    Button btnTimePick;
    Spinner spCallType;
    EditText mNumberView;
    EditText mDurationView;
    Switch mVideoSwitch;
    //    TextView dateDisplay;
    final int DATE_DIALOG = 1;
    final int TIME_DIALOG = 2;
    final int REQUEST_CODE_ASK_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDatePick = (Button) findViewById(R.id.dateChoose);
        btnTimePick = (Button) findViewById(R.id.timeChoose);
//        dateDisplay = (TextView) findViewById(R.id.dateDisplay);
        spCallType = (Spinner) findViewById(R.id.spCallType);
        mNumberView = (EditText) findViewById(R.id.etNumber);
        mDurationView = (EditText) findViewById(R.id.etCallDuration);
        mVideoSwitch = (Switch) findViewById(R.id.switch_video);
        if (Build.VERSION.SDK_INT < 21) {
            mVideoSwitch.setVisibility(View.GONE);
        }

        btnDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG);
            }
        });
        btnTimePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG);
            }
        });
        spCallType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCallType = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        mHour = ca.get(Calendar.HOUR_OF_DAY);
        mMinute = ca.get(Calendar.MINUTE);

        mCallType = 1;

        displayDate();
        displayTime();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG:
                return new DatePickerDialog(this, mdateListener, mYear, mMonth, mDay);
            case TIME_DIALOG:
                return new TimePickerDialog(this, mTimeListener, mHour, mMinute, true);
        }
        return null;
    }

    /**
     * 设置日期 利用StringBuffer追加
     */
    public void displayDate() {
//        dateDisplay.setText(new StringBuffer().append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" "));
        btnDatePick.setText(new StringBuffer().append(mYear).append("-")
                .append(mMonth < 9 ? "0" : "").append(mMonth + 1).append("-")
                .append(mDay > 9 ? "" : "0").append(mDay));
    }

    public void displayTime() {
        btnTimePick.setText(new StringBuffer().append(mHour).append(":").append(mMinute > 9 ? mMinute : "0" + mMinute));
    }

    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            displayDate();
        }
    };

    TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            displayTime();
        }
    };

    public void myClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                insertCallLog();
                break;
            default:
                break;
        }
    }

    public void insertCallLog() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWriteCallLogPermission = checkSelfPermission(Manifest.permission.WRITE_CALL_LOG);
            if (hasWriteCallLogPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALL_LOG)) {
                    showMessageOKCancel("You need to allow access to CallLog",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        // check if null
        if (TextUtils.isEmpty(mNumberView.getText())) {
            Toast.makeText(MainActivity.this, "号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp t;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        try {
            t = new Timestamp(format.parse(btnDatePick.getText() + " " + btnTimePick.getText()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            t = new Timestamp(new Date().getTime());
        }

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, mNumberView.getText().toString());
        values.put(CallLog.Calls.TYPE, mCallType);
        values.put(CallLog.Calls.DATE, t.getTime());
        values.put(CallLog.Calls.DURATION, TextUtils.isEmpty(mDurationView.getText()) ? "0" : mDurationView.getText().toString());
        if (Build.VERSION.SDK_INT >= 21 && mVideoSwitch.isChecked()) {
            values.put(CallLog.Calls.FEATURES, CallLog.Calls.FEATURES_VIDEO);
        }
        Uri result = null;
        if (Build.VERSION.SDK_INT < 21) {
            result = getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        } else {
            try {
                result = getContentResolver().insert(CallLog.Calls.CONTENT_URI_WITH_VOICEMAIL, values);
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "操作失败：权限存在问题", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (result != null) {
            Toast.makeText(MainActivity.this, "操作成功：" + result, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    insertCallLog();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "WRITE_CALL_LOG Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
