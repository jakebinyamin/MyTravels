package com.jbsw.mytravels;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.jbsw.data.DBManager;
import com.jbsw.data.GpsDataTable;
import com.jbsw.data.NotesTable;
import com.jbsw.data.PhotoLinkTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.GpsTracker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager.widget.ViewPager;

import static java.security.AccessController.getContext;

public class TripActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener
{
    private static final String TAG = "TagTripActivity";

    private TabLayout m_Tabs;
    public ViewPager m_ViewPager;
    private TabItem m_TabGeneral, m_TabJournal, m_TabMap;
    public TripPageAdapter m_PageAdapter;
    private ImageButton m_BtnBack, m_BtnMenu;

    private TravelMasterTable.DataRecord m_DR;
    private static int REQUEST_PHOTO = 1;

    TravelMasterTable.DataRecord GetDataRecord() { return m_DR; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        long id =  getIntent().getLongExtra("DATARECORD", -1);
        TravelMasterTable tab = new TravelMasterTable();
        m_DR = tab.QueryRecord(id);
        if (m_DR != null)
        {
            TextView Title = findViewById(R.id.title_text);
            Title.setText(m_DR.Name);
        }

        m_BtnBack = (ImageButton) findViewById(R.id.back);
        m_BtnBack.setOnClickListener(this);
        m_BtnMenu = (ImageButton) findViewById(R.id.menu);
        m_BtnMenu.setOnClickListener(this);

        m_Tabs = (TabLayout) findViewById(R.id.trip_main_tab);
        m_TabGeneral = (TabItem) findViewById(R.id.tab_general);
        m_TabJournal = (TabItem) findViewById(R.id.tab_journal);
        m_TabMap = (TabItem) findViewById(R.id.tab_map);
        m_ViewPager = (ViewPager) findViewById(R.id.view_pager);

        m_PageAdapter = new TripPageAdapter(getSupportFragmentManager(), m_Tabs.getTabCount());
        m_ViewPager.setAdapter(m_PageAdapter);
        m_ViewPager.setOffscreenPageLimit(4);
        m_Tabs.addOnTabSelectedListener(new TabSelectedListener());
        m_ViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(m_Tabs));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause...");
        UpdateData();
        Log.d(TAG, "onPause - Update Data complete...");
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v)
    {
        if (v == m_BtnBack)
        {
            Log.d(TAG, "m_BtnBack Pressed...");
            finish();
            Log.d(TAG, "TripActivity finish() called...");
        }

        if (v== m_BtnMenu)
        {
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.trip_menu);
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                PromptForDelete();
                break;
            case R.id.menu_change_photo:
                SelectPhoto();
                break;
            case R.id.menu_view_photos:
                LoadPhotoViewer();
                break;

            default:
                return false;
        }
        return true;
    }

    private void SelectPhoto()
    {
        ArrayList<String> PhotoList = HasPhoto();
        if (PhotoList == null)
            return;

        Intent photoIntent = new Intent(this, PhotoViewer.class);
        photoIntent.putStringArrayListExtra(PhotoViewer.IntentData, PhotoList);
        photoIntent.putExtra(PhotoViewer.IntentSelectMode, true);
        startActivityForResult(photoIntent, REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PHOTO || resultCode != PhotoViewer.RESULT_OK)
            return;

        String sPhoto = data.getStringExtra(PhotoViewer.IntentSelectedPhoto);
        TravelMasterTable Master = new TravelMasterTable();
        Master.UpdatePhoto(m_DR.Id, sPhoto);
    }

    private void LoadPhotoViewer()
    {
        ArrayList<String> PhotoList = HasPhoto();
        if (PhotoList == null)
            return;

        Intent photoIntent = new Intent(this, PhotoViewer.class);
        photoIntent.putStringArrayListExtra(PhotoViewer.IntentData, PhotoList);
        startActivity(photoIntent);
    }

    private ArrayList<String> HasPhoto()
    {
        ArrayList<String> PhotoList = new ArrayList<String>();
        PhotoLinkTable Photos = new PhotoLinkTable();

        if (Photos.QueryForMaster(m_DR.Id) == -1)
        {
            Toast toast = Toast.makeText(this, R.string.no_photo, Toast.LENGTH_LONG);
            toast.show();
            return null;
        }

        PhotoLinkTable.DataRecord PhotoDr;
        while ((PhotoDr = Photos.GetNextRecord()) != null)
            PhotoList.add(PhotoDr.sPath);

        return PhotoList;
    }

    private void PromptForDelete()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.ru_sure_delete_trip);
        builder1.setCancelable(true);

        builder1.setNeutralButton(
                R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        DBManager DBM = DBManager.Get();
                        DBM.DeleteTrip(m_DR.Id);
                        // TODO Add a Toast
                        finish();
                    }
                });

        builder1.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void UpdateData()
    {
        TabGeneral tGeneral = (TabGeneral)m_PageAdapter.GetFragment(TripPageAdapter.TAB_GENERAL);
        m_DR = tGeneral.GetDataRecord();
        TravelMasterTable tab = new TravelMasterTable();
        tab.UpdateRecord(m_DR);
    }

    private class TabSelectedListener implements OnTabSelectedListener
    {
        @Override
        public void onTabSelected(TabLayout.Tab tab)
        {
            m_ViewPager.setCurrentItem(tab.getPosition());
            Log.d(TAG, "In TabSelectedListener");
//            m_PageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab)
        {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab)
        {

        }
    }
}
