package com.jbsw.mytravels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jbsw.data.DBManager;
import com.jbsw.data.NotesTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Utils;

import java.text.ParseException;

// TODO Add background when no trips
// TODO Delete Trip
// TODO Menu and about

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = "TagMainActivity";
    private FloatingActionButton m_BtnCreate;
    private ImageButton m_BtnOpenDrawer;
    private ImageButton m_BtnSettings;
    private ListView m_List;
    private MasterDBAdapter m_DBAdapter;
    private DBManager m_DBM = null;
    private DrawerLayout m_Drawer;
    NavigationView m_NavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.main_drawer);
        LinearLayout LayoutIntro = findViewById(R.id.no_records);
        LayoutIntro.setVisibility(View.GONE);

        m_BtnCreate = (FloatingActionButton)findViewById(R.id.create_project);
        if (m_BtnCreate != null)
            m_BtnCreate.setOnClickListener(this);

        m_BtnOpenDrawer = findViewById(R.id.btn_open_draw);
        m_BtnOpenDrawer.setOnClickListener(this);
        m_Drawer = findViewById(R.id.main_drawer);
        m_NavigationView = (NavigationView)findViewById(R.id.nav_view);
        m_NavigationView.setNavigationItemSelectedListener(this);
        m_NavigationView.bringToFront();
        ImageView Img = (ImageView) findViewById(R.id.image);
        Img.setClipToOutline(true);

        m_BtnSettings = (ImageButton) findViewById(R.id.btn_settings);
        m_BtnSettings.setOnClickListener(this);

        //m_DBM = new DBManager(this);
        m_DBM = DBManager.Create(getApplicationContext());
        Utils.SetContext(getApplicationContext());

        m_List = (ListView) findViewById(R.id.project_list);
        m_DBAdapter = new MasterDBAdapter(this);
        m_List.setAdapter(m_DBAdapter);
        m_List.setOnItemClickListener(new ItemSelectedListener());

//        SetupUX();

        //
        // Start the gps receiver
        Intent MyIntent = new Intent(this, GpsTracker.class);
        this.startService(MyIntent);

        Utils.RegisterGpsReceiver();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.menu_settings:
                LaunchSettings();
                break;
            case R.id.menu_intro:
                Intent intentIntro = new Intent(this, Intro.class);
                startActivity(intentIntro);
                break;
            case R.id.menu_privacy:
                LaunchPrivacyPolicy();
                break;
        }

        m_Drawer.closeDrawer(Gravity.LEFT);
        return false;
    }

    private void LaunchPrivacyPolicy()
    {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://jbswdev.wixsite.com/mitravels/privacy-policy"));
        startActivity(myIntent);
    }

    private void LaunchSettings()
    {
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        startActivity(intentSettings);
    }
    private class ItemSelectedListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            TravelMasterTable.DataRecord DR = (TravelMasterTable.DataRecord) m_DBAdapter.getItem(position);
            Log.d(TAG, "Name: " + DR.Name);
            Intent intent = new Intent(MainActivity.this, TripActivity.class);
            intent.putExtra("DATARECORD", DR.Id);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_DBAdapter.Refresh();
        SetupUX();
    }

    public void SetupUX()
    {
        LinearLayout LayoutIntro = findViewById(R.id.no_records);
        LayoutIntro.setVisibility(m_DBAdapter.RecordsExist() ? View.GONE : View.VISIBLE);
        m_List.setVisibility(m_DBAdapter.RecordsExist() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Utils.UnRegisterGpsReceiver();
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnCreate)
        {
            Intent intent = new Intent(this, CreateProject.class);
            startActivity(intent);
        }

       if (v == m_BtnOpenDrawer)
       {
            m_Drawer.openDrawer(GravityCompat.START);
       }

       if (v == m_BtnSettings)
       {
           LaunchSettings();
       }
    }
}
