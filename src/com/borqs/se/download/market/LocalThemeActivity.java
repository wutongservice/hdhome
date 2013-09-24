package com.borqs.se.download.market;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.borqs.freehdhome.R;
import com.borqs.market.json.Product.SupportedMod;
import com.borqs.market.utils.MarketUtils;
import com.borqs.se.home3d.SEHomeUtils;
import com.umeng.analytics.MobclickAgent;

public class LocalThemeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.theme_local);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.themes);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(LocalThemeActivity.this, OnlineThemeActivity.class);
//                startActivity(intent);
                MarketUtils.startProductListIntent(LocalThemeActivity.this,
                        MarketUtils.CATEGORY_THEME, true,SupportedMod.PORTRAIT);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
