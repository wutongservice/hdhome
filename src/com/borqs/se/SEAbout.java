package com.borqs.se;

import com.borqs.freehdhome.R;
import com.borqs.se.home3d.BackDoorSettingsActivity;
import com.borqs.se.home3d.SEHomeUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SEAbout extends Activity {
    private TextView versionTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        versionTV = (TextView) findViewById(R.id.version);
        String packageName = getPackageName();
        String version = "0.1.1(1001)";
        try {
            version = getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        versionTV.setText(String.format(SEAbout.this.getString(R.string.about_version), String.valueOf(version)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// hack code to open backdoor
        if (null != versionTV) {
            BackDoorSettingsActivity.installBackDoor(this);
            versionTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (BackDoorSettingsActivity.knockAtBackDoor(getApplicationContext())) {
                        onBackdoorOpen();
                    }
                }
            });
        }
        /// end of back door
    }

    private void onBackdoorOpen() {
        finish();
    }
}
