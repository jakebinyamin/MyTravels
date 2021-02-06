package com.jbsw.mytravels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbsw.utils.Prefs;

public class Intro extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener
{
    private static final String TAG = "TAGIntro";
    private ViewPager m_Viewpager;
    private LinearLayout m_Linear;

    private TextView[] m_Dots;
    private Button m_BtnNext,m_BtnBack;
    private IntroPageAdapter m_Adapter;

    private int m_CurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        m_Viewpager=(ViewPager)findViewById(R.id.viewpager);
        m_Linear=(LinearLayout)findViewById(R.id.dots);
        m_Viewpager.addOnPageChangeListener(this);

        m_Adapter = new IntroPageAdapter(this);
        m_Viewpager.setAdapter(m_Adapter);

        m_BtnNext=(Button)findViewById(R.id.intro_next);
        m_BtnNext.setOnClickListener(this);
        m_BtnBack=(Button)findViewById(R.id.intro_prev);
        m_BtnBack.setOnClickListener(this);

        adddots(0);
    }

    public void adddots(int i)
    {
        m_Dots=new TextView[5];
        m_Linear.removeAllViews();

        for (int x=0;x<m_Dots.length;x++) {

            m_Dots[x]=new TextView(this);
            m_Dots[x].setText(Html.fromHtml("&#8226;"));
            m_Dots[x].setTextSize(35);
            m_Dots[x].setTextColor(getResources().getColor(R.color.colorGrey));

            m_Linear.addView(m_Dots[x]);
        }
        if (m_Dots.length>0) {
            m_Dots[i].setTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnNext)
        {
            Log.d(TAG, "Next Pressed, m_CurrentPage is " + m_CurrentPage + ", m_Adapter.getCount() " + m_Adapter.getCount());
            if (m_CurrentPage < m_Adapter.getCount()-1) {
                Log.d(TAG, "Next Pressed, Doing next page " + m_CurrentPage);
                m_Viewpager.setCurrentItem(m_CurrentPage + 1);
            }
            else {
                Log.d(TAG, "Next Pressed, Showing next screen " + m_CurrentPage);
                ShowNextScreen();
            }
        }
        if (v == m_BtnBack)
        {
            Log.d(TAG, "Back Pressed, m_CurrentPage is " + m_CurrentPage);
            if (m_CurrentPage > 0)
                m_Viewpager.setCurrentItem(m_CurrentPage-1);
        }
    }

    private void ShowNextScreen()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Prefs prefs = new Prefs(this);
        prefs.SetIntroShown();
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
        Log.d(TAG, "onPageSelected, position: " + position);
        adddots(position);
        m_CurrentPage = position;

        if (position == m_Adapter.getCount()-1)
        {
            m_BtnNext.setText(R.string.lets_start);
        }
        else
            m_BtnNext.setText(R.string.next);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class IntroPageAdapter extends PagerAdapter
    {
        Context m_Context;
        LayoutInflater m_Inflater;
        private final String[] m_Titles;
        private final String[] m_Descriptions;
        private final int[] m_Images = {
            R.drawable.intro0,
            R.drawable.intro1,
            R.drawable.intro2,
            R.drawable.intro3,
            R.drawable.intro4
        };
        private final int[] m_ListColor = {
            R.color.colorBkg6,
            R.color.colorBkg7,
            R.color.colorBkg6,
            R.color.colorBkg9,
            R.color.colorBkg5,
        };

        public IntroPageAdapter(Context context)
        {
            m_Titles = getResources().getStringArray(R.array.intro_titles);
            m_Descriptions = getResources().getStringArray(R.array.intro_details);
            this.m_Context = context;
        }

        @Override
        public int getCount()
        {
            return m_Titles.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
        {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            Log.d(TAG, "Instantiating item: " + position + ", " + m_Titles[position]);
            m_Inflater = (LayoutInflater)m_Context.getSystemService(m_Context.LAYOUT_INFLATER_SERVICE);
            View view = m_Inflater.inflate(R.layout.intro_page,container,false);

            View linearLayout = view.findViewById(R.id.intro_layout);
            ImageView img = (ImageView)view.findViewById(R.id.image);
            TextView txt1 = (TextView) view.findViewById(R.id.title);
            TextView txt2 = (TextView) view.findViewById(R.id.description);


            img.setImageResource(m_Images[position]);
            img.setClipToOutline(true);
            txt1.setText(m_Titles[position]);
            txt2.setText(m_Descriptions[position]);
            int ActualColor = ContextCompat.getColor(m_Context, m_ListColor[position]);
            linearLayout.setBackgroundColor(ActualColor);

            container.addView(view);

            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View)object);
            Log.d(TAG, "destroyItem: " + position + ", " + m_Titles[position]);
        }
    }
}