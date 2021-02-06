package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jbsw.utils.Utils;

public class EditJournalLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener
{
    private GoogleMap m_Map;
    private MapView m_MapView;
    private LatLng m_CurrLoc;
    private int m_Type;
    private ImageButton m_BtnCancel;
    private Button m_BtnSave;

    public final static String DataType = "DataTtype";
    public final static String DataLong = "DataLong";
    public final static String DataLat = "DataLat";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal_location);

        m_MapView = findViewById(R.id.mapView);
        if (m_MapView != null) {
            m_MapView.onCreate(null);
            m_MapView.onResume();
            m_MapView.getMapAsync(this);
        }

        m_BtnCancel = findViewById(R.id.cancel);
        m_BtnCancel.setOnClickListener(this);
        m_BtnSave = findViewById(R.id.save);
        m_BtnSave.setOnClickListener(this);

        m_Type = getIntent().getIntExtra(DataType, 0);
        Double nLong = getIntent().getDoubleExtra(DataLong, -1);
        Double nLat = getIntent().getDoubleExtra(DataLat, -1);
        m_CurrLoc = new LatLng(nLat, nLong);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        m_MapView.onResume();
    }
        @Override
    public void onMapReady(GoogleMap googleMap)
    {
        MapsInitializer.initialize(this);
        m_Map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        m_Map.setOnMapClickListener(this);

        BuildMap();
    }

    private void BuildMap()
    {
        m_Map.clear();
        final int IconList[] = Utils.GetMapList();

//        Drawable d = getResources().getDrawable(m_Type, getTheme());
        Drawable d = ContextCompat.getDrawable(this, IconList[m_Type]);
        Bitmap bm = Utils.drawableToBitmap(d);
        Bitmap icon = Bitmap.createScaledBitmap(bm, 100, 100, true);

        m_Map.addMarker(new MarkerOptions().position(m_CurrLoc).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        m_Map.animateCamera(CameraUpdateFactory.newLatLngZoom(m_CurrLoc, 17.0f));
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        Log.d("TAGMapJake", "Got Here");
        m_CurrLoc = latLng;
        BuildMap();
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnSave)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(DataLong, m_CurrLoc.longitude);
            returnIntent.putExtra(DataLat, m_CurrLoc.latitude);
            setResult(RESULT_OK, returnIntent);
        }

        finish();
    }
}