package com.jbsw.mytravels;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbsw.data.NotesTable;
import com.jbsw.data.PhotoLinkTable;
import com.jbsw.utils.Utils;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class JournalActivityBase extends AppCompatActivity implements PhotoListViewAdapter.PhotoItemClickListener//, View.OnClickListener
{
    protected long m_Id;
    protected long m_Node_id;
    protected NotesTable m_Table;
    protected NotesTable.DataRecord m_DR = null;
    protected int m_Mode;

    protected ImageButton m_BtnCancel;
    protected ArrayList<String> m_PhotoList = null;
    protected PhotoListViewAdapter m_PhotoListAdapter;

    public static final String PARAM_NODE_ID = "NODE_ID";
    public static final String PARAM_MASTER_ID = "MASTER_ID";
    public static final String PARAM_MODE = "DATAMODE";

    protected static int REQUEST_PHOTO = 1;
    protected static int EDIT_LOCATION = 2;

    protected static final int ViewPhotoMode = 1;
    protected static final int RemovePhotoMode = 2;
    protected int m_PhotoViewingMode = ViewPhotoMode;

    protected void GetInputParams()
    {
        m_Id = getIntent().getLongExtra(PARAM_MASTER_ID, -1);
        m_Node_id = getIntent().getLongExtra(PARAM_NODE_ID, -1);
        m_Mode = getIntent().getIntExtra(PARAM_MODE, -1);
    }

    protected void Initialise()
    {
        m_BtnCancel = (ImageButton) findViewById(R.id.cancel);
        m_PhotoList = new ArrayList<String>();

        RecyclerView recyclerView = findViewById(R.id.photo_list);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        m_PhotoListAdapter = new PhotoListViewAdapter(m_PhotoList);
        recyclerView.setAdapter(m_PhotoListAdapter);
        m_PhotoListAdapter.setPhotoClickListener(JournalActivityBase.this);

        m_Table = new NotesTable();
    }


    protected void LoadRecord()
    {
        m_DR = m_Table.GetJournalEntry(m_Node_id);
        if (m_DR == null)
            return; // TODO need to deal with this error (cant find Note record)

        //
        // Title
        TextView sTitle = (TextView) findViewById(R.id.journal_title);
        sTitle.setText(m_DR.sTitle);

        //
        // Body
        TextView sBody = (TextView) findViewById(R.id.journal_entry);
        sBody.setMovementMethod(new ScrollingMovementMethod());
        sBody.setText(m_DR.sStringNote);

//        //
//        // Set icon
//        ImageView Img = (ImageView) findViewById(R.id.type_icon);
//        final int IconList[] = Utils.GetIconList();
//        Img.setImageResource(IconList[m_DR.nType]);

        //
        // Date
        TextView Date1 = (TextView) findViewById(R.id.date);
        if (Date1 != null) {
            String sOut = Utils.GetReadableStringDateWithTime(m_DR.Date);
            Date1.setText(sOut);
        }

        //
        // Load Photos
        PhotoLinkTable TabPhotos = new PhotoLinkTable();
        if (TabPhotos.QueryForNoteEntry(m_DR.Id) == -1)
            return;
        PhotoLinkTable.DataRecord PhotoDr;
        while ((PhotoDr = TabPhotos.GetNextRecord()) != null)
            m_PhotoList.add(PhotoDr.sPath);
    }

    @Override
    public void onPhotoItemClick(View view, int position)
    {
        Intent photoIntent = new Intent(this, PhotoViewer.class);
        photoIntent.putStringArrayListExtra(PhotoViewer.IntentData, m_PhotoList);
        photoIntent.putExtra(PhotoViewer.IntentPosn, position);
        if (m_PhotoViewingMode == RemovePhotoMode)
            photoIntent.putExtra(PhotoViewer.IntentRemoveMode, true);
//        if (m_PhotoViewingMode == ViewPhotoMode)
//            photoIntent.putExtra(PhotoViewer.IntentViewOnlyMode, true);

        startActivityForResult(photoIntent, REQUEST_PHOTO);
    }
}
