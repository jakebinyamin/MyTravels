package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener
{
    private ImageButton m_BtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        m_BtnCancel = (ImageButton) findViewById(R.id.cancel);
        m_BtnCancel.setOnClickListener(this);

        SetVersion();
    }

    private void SetVersion()
    {
        String versionNumber = "";
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // extract the build number from the versionName
        if (info != null)
            versionNumber = info.versionName;

        TextView tv = (TextView) findViewById(R.id.version_title);
        String sVsn = String.format(getResources().getString(R.string.version_s), versionNumber);
        tv.setText(sVsn);
    }

    @Override
    public void onClick(View v)
    {
        finish();
    }
}