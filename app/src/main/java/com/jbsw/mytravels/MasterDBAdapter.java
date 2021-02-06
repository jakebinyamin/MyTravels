package com.jbsw.mytravels;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbsw.data.PhotoLinkTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.PhotoBackgroundLoader;
import com.jbsw.utils.Utils;

import org.w3c.dom.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class MasterDBAdapter extends BaseAdapter
{
    private static final String TAG = "TAGMasterDBAdapter";
    private TravelMasterTable m_MasterTable;
    private Context m_Context;
    private LayoutInflater m_inflter;
    private PhotoBackgroundLoader m_BkgLoader;
    private boolean m_bRecordsExist;

    public MasterDBAdapter(Context context)
    {
        m_inflter = (LayoutInflater.from(context));
        m_Context = context;
        m_MasterTable = new TravelMasterTable();
        m_BkgLoader = new PhotoBackgroundLoader();
        m_BkgLoader.SetNotLoadedResource(R.drawable.photo_loading_bkg);
        m_BkgLoader.LoadDefaultBitmap(context, R.drawable.splash);
        Refresh();
    }

    public void Refresh()
    {
        m_bRecordsExist = m_MasterTable.QueryALl();
    }

    public boolean RecordsExist()
    {
        return m_bRecordsExist;
    }

    @Override
    public int getCount()
    {
        return m_MasterTable.GetRecordCount();
    }

    @Override
    public Object getItem(int position)
    {
        TravelMasterTable.DataRecord DR = m_MasterTable.GetDataAtPosition(position);
        return DR;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    private class ViewHolder
    {
        TextView txtName;
        TextView txtLine2;
        TextView txtLine3;
        TextView txtLine4;
        ImageView Icon;
        ImageView Photo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            Log.d(TAG, "ConvertView for position: " + position);
            convertView = m_inflter.inflate(R.layout.master_record_row, null);
            convertView.setClipToOutline(true);
            ViewHolder v = new ViewHolder();
            v.txtName = (TextView) convertView.findViewById(R.id.project_name);
            v.txtLine2 = (TextView)convertView.findViewById(R.id.line2);
            v.txtLine3 = (TextView)convertView.findViewById(R.id.line3);
            v.txtLine4 = (TextView)convertView.findViewById(R.id.status);
            v.Icon = (ImageView) convertView.findViewById(R.id.status_icon);
            v.Photo = (ImageView)convertView.findViewById(R.id.photo);
            convertView.setTag(v);
        }

        int Backgrounds[] = {R.drawable.master_rec_bacground2, R.drawable.master_rec_background3, R.drawable.master_rec_background4, R.drawable.master_rec_background5};
        int pos = position % Backgrounds.length;
        convertView.setBackgroundResource(Backgrounds[pos]);

        ViewHolder v = (ViewHolder) convertView.getTag();
        TravelMasterTable.DataRecord DR = m_MasterTable.GetDataAtPosition(position);
        if (DR == null)
            return convertView;

        v.txtName.setText(DR.Name);
        v.txtLine2.setText(GetLine2Text(DR));
        v.txtLine3.setText(GetLine3Text(DR));
        v.txtLine4.setText(GetLine4Text(DR, convertView));

        SetIcon(DR, v.Icon);

        if (DR.sPhoto != null && !DR.sPhoto.isEmpty())
        {
            Log.d(TAG, "From File, id: " + DR.Id + " sPhoto is: " + DR.sPhoto);
            m_BkgLoader.LoadPhoto(DR.sPhoto, v.Photo);
        }
        else
        {
            Log.d(TAG, "From Resource " + DR.Id);
            m_BkgLoader.LoadDefaultPhoto(v.Photo);
        }

        return convertView;
    }

    private void SetIcon(TravelMasterTable.DataRecord DR, ImageView Img)
    {
        Img.clearAnimation();
        if (DR.Status == TravelMasterTable.StatusInProgress)
        {
            Utils.SetRecordingAnimation(Img);
//            Img.setImageResource(R.drawable.recording);
//            Animation mAnimation = new AlphaAnimation(1, 0);
//            mAnimation.setDuration(500);
//            mAnimation.setInterpolator(new LinearInterpolator());
//            mAnimation.setRepeatCount(Animation.INFINITE);
//            mAnimation.setRepeatMode(Animation.REVERSE);
//            Img.startAnimation(mAnimation);
        }
        if (DR.Status == TravelMasterTable.StatusComplete)
            Img.setImageResource(R.drawable.tick);
        if (DR.Status == TravelMasterTable.StatusNotStarted)
            Img.setImageResource(R.drawable.play);

    }

    private String GetLine2Text(TravelMasterTable.DataRecord DR)
    {
        if (DR.Status == TravelMasterTable.StatusNotStarted)
        {
            return "";
//            String s = m_Context.getResources().getString(R.string.not_started);
//            return s;
        }

        String sDate = Utils.GetReadableStringDate(DR.StartDate);
        return String.format(m_Context.getResources().getString(R.string.date_started), sDate);
    }

    private String GetLine3Text(TravelMasterTable.DataRecord DR)
    {
        String sOut = "";
        if (DR.Status == TravelMasterTable.StatusInProgress)
        {
            LocalDate dateNow = LocalDate.now();
            String sNow = String.format("%d-%02d-%02d", dateNow.getYear(), dateNow.getMonthValue(), dateNow.getDayOfMonth());
            long nDays = Utils.CalculateDays(DR.StartDate, sNow);
            sOut = String.format(m_Context.getResources().getString(R.string.going_for), nDays);
        }

        if (DR.Status == TravelMasterTable.StatusComplete)
        {
            String sDate = Utils.GetReadableStringDate(DR.EndDate);
            sOut = String.format(m_Context.getResources().getString(R.string.date_ended), sDate);
        }

        return sOut;
    }

    private String GetLine4Text(TravelMasterTable.DataRecord DR, View v)
    {
        String StatusList[] = v.getResources().getStringArray(R.array.trip_status_array);
        return StatusList[DR.Status];
    }
}
