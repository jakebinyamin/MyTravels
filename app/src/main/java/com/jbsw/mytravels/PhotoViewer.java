package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;

// TODO ability to remove photos and return the result

public class PhotoViewer extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener
{
    private static final String TAG = "TAGPhotoViewer";
    private ImageButton m_BtnBack;
    private Button m_BtnSelect, m_BtnRemove;
    private ViewPager m_ImageViewPager;
    private ImageSliderAdapter m_SlideAdapter;
    private int m_Position;
    private ArrayList<String> m_PhotoList = null;

    public final static String IntentData = "PhotoList";
    public final static String IntentSelectMode = "SelectPhoto";
    public final static String IntentRemoveMode = "RemovePhoto";
    public final static String IntentViewOnlyMode = "ViewOnly";
    public final static String IntentPosn = "PhotoListPos";
    public final static String IntentSelectedPhoto = "SelectedPhoto";
    public final static int RESULT_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        LoadInThread Thrd = new LoadInThread();
        Thrd.execute();

//        Initialise();
    }

    private class LoadInThread extends AsyncTask<Object,Void,String>
    {
//        private ProgressDialog dialog;
//        @Override
//        protected void onPreExecute()
//        {
//            dialog = new ProgressDialog(PhotoViewer.this);
//            dialog.setMessage(getString(R.string.loading_map));
//            dialog.show();
//        }

        @Override
        protected String doInBackground(Object... objects) {
            Initialise();
            return null;
        }
//        @Override
//        protected void onPostExecute(String str)
//        {
//            if (dialog.isShowing())
//                dialog.dismiss();
//        }
    }


    private void Initialise()
    {
        m_BtnBack = (ImageButton) findViewById(R.id.back);
        m_BtnBack.setOnClickListener(this);

        m_BtnSelect = (Button) findViewById(R.id.select);
        m_BtnSelect.setOnClickListener(this);
        boolean bUseButton = getIntent().getBooleanExtra(IntentSelectMode, false);
        m_BtnSelect.setVisibility(bUseButton ? View.VISIBLE : View.GONE );

        m_BtnRemove = (Button) findViewById(R.id.remove);
        m_BtnRemove.setOnClickListener(this);
        bUseButton = getIntent().getBooleanExtra(IntentRemoveMode, false);
        m_BtnRemove.setVisibility(bUseButton ? View.VISIBLE : View.GONE );

        m_PhotoList = getIntent().getStringArrayListExtra(IntentData);
        int nPos = getIntent().getIntExtra(IntentPosn, 0);

        //
        // Setup Photo Adapter
        m_ImageViewPager = (ViewPager) findViewById(R.id.image_slider);
        m_ImageViewPager.addOnPageChangeListener(this);
        m_SlideAdapter = new ImageSliderAdapter(getSupportFragmentManager());
        m_ImageViewPager.setAdapter(m_SlideAdapter);


        //
        // Add Photos to Fragment
        int nCount = m_PhotoList.size();
        for (int i= 0; i < nCount; i++)
            AddPhotoToFragment(m_PhotoList.get(i));
        m_SlideAdapter.notifyDataSetChanged();
        m_ImageViewPager.setCurrentItem(nPos, false);
    }

    void AddPhotoToFragment(String sPhoto)
    {
        Fragment Frag = ImageSliderFragment.newInstance(sPhoto);
        m_SlideAdapter.addFragment(Frag);
    }


    @Override
    public void onClick(View v)
    {
        if (v == m_BtnBack)
        {
            Log.d(TAG, "Back pressed");
            finish();
        }

        if (v == m_BtnSelect || v == m_BtnRemove)
        {
            ReturnSelectedPhoto();
        }
    }

    private void ReturnSelectedPhoto()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(IntentPosn, m_Position);
        returnIntent.putExtra(IntentSelectedPhoto, m_PhotoList.get(m_Position));
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // ViewPager implementations
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG,"Page changed.. " + position);
        m_Position = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
    /////////////////////////////////////////////////////////////////////////////////////////
}
