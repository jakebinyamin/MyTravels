package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbsw.data.NotesTable;
import com.jbsw.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class ViewJournal extends JournalActivityBase implements View.OnClickListener//, PhotoListViewAdapter.PhotoItemClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journal);

        GetInputParams();
        Initialise();
        m_PhotoViewingMode = ViewPhotoMode;

        m_BtnCancel.setOnClickListener(this);
        LoadRecord();
    }

    @Override
    protected void LoadRecord()
    {
        super.LoadRecord();

        //
        // Set icon
        ImageView Img = (ImageView) findViewById(R.id.type_icon);
        final int IconList[] = Utils.GetIconList();
        Img.setImageResource(IconList[m_DR.nType]);

        RecyclerView recyclerView = findViewById(R.id.photo_list);
        if (m_PhotoList.isEmpty())
            recyclerView.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v)
    {
        if (v == m_BtnCancel)
        {
            finish();
            return;
        }
    }

}