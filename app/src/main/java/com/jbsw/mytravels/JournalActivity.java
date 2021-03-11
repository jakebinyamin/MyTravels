package com.jbsw.mytravels;
  
  
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jbsw.data.DBManager;
import com.jbsw.data.NotesTable;
import com.jbsw.data.NotesTable.DataRecord;
import com.jbsw.data.PhotoLinkTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Prefs;
import com.jbsw.utils.Utils;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

// TODO Add support for voice notes
// TODO Clean up view for editing
// TODO Some text when there are no photos
// TODO Menu
// TODO Menu option for editing GPS location
// TODO Option to hide GPS location
// TODO Deleting photo

public class JournalActivity extends JournalActivityBase implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "TAGJournalActivity";
    private Button m_BtnCreate;
    private ImageButton m_BtnAddPhoto;
    private ImageButton m_BtnMenu;
    private Spinner m_Spinner;
    private String m_sMainPhoto;
    private CheckBox m_ChkBoxShowOnMap;

    private static final int PICK_IMAGE = 100;

    public static int ModeNewRecord = 1;
    public static int ModeEdit = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        GetInputParams();
        Initialise();

        m_sMainPhoto = null;

        m_PhotoViewingMode = RemovePhotoMode;

        if (m_Mode == ModeEdit)
            LoadRecord();
        else
            m_DR = m_Table.GetNewDataRecord();

        //
        // Setup buttons
        m_BtnCreate = (Button) findViewById(R.id.create);
        m_BtnCreate.setOnClickListener(JournalActivity.this);
        m_BtnCancel.setOnClickListener(JournalActivity.this);
        m_BtnAddPhoto = (ImageButton) findViewById(R.id.addphoto);
        m_BtnAddPhoto.setOnClickListener(JournalActivity.this);
        m_BtnMenu = (ImageButton) findViewById(R.id.menu);
        m_BtnMenu.setOnClickListener(JournalActivity.this);
        m_ChkBoxShowOnMap = (CheckBox) findViewById(R.id.show_on_map);
        m_ChkBoxShowOnMap.setChecked(m_DR.bShowOnMap);

        m_Spinner = findViewById(R.id.journal_type);
        JournalTypeAdapter adapter = new JournalTypeAdapter(this, R.layout.journal_type);
        m_Spinner.setAdapter(adapter);
        m_Spinner.setSelection(m_DR.nType);
    }

    @Override
    protected void LoadRecord()
    {
        super.LoadRecord();
    }

    @Override
    public void onClick(View v) {
        if (v == m_BtnCancel) {
            finish();
            return;
        }

        if (v == m_BtnMenu) {
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.journal_menu);
            popup.setOnMenuItemClickListener(JournalActivity.this);
            popup.show();
            return;
        }

        if (v == m_BtnCreate) {
            if (m_Mode == ModeNewRecord)
                CreateRecord();
            if (m_Mode == ModeEdit)
                UpdateRecord();

            finish();
            return;
        }

        if (v == m_BtnAddPhoto) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE);
                } else {
                    PickPhotos();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PICK_IMAGE) {
            // If request is cancelled, the result arrays are empty.
            Log.d(TAG, "Permission for reading photos.." + grantResults[0]);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission for reading photos.." + grantResults[0] + " VERIFIED.");
                PickPhotos();
            }
        }
    }

    private void PickPhotos()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI); //EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO && resultCode == PhotoViewer.RESULT_OK) {
            int nPos = data.getIntExtra(PhotoViewer.IntentPosn, -1);
            if (nPos == -1)
                return;

            PhotoLinkTable Tab = new PhotoLinkTable();
            Tab.DeletePhotoFromNote(m_DR.Id, m_PhotoList.get(nPos));
            m_PhotoList.remove(nPos);
            m_PhotoListAdapter.UpdateList(m_PhotoList);
            m_PhotoListAdapter.notifyDataSetChanged();
        }

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            if (data == null)
                return;

            //
            // Multiple photos selected..
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    ProcessPhoto(imageUri);
                    Log.d(TAG, "data.getClipData() != null - Adding Photo..");
                }
                return;
            }

            //
            // Single photo selected..
            if (data.getData() != null) {
                Uri imageUri = data.getData();
                ProcessPhoto(imageUri);
                Log.d(TAG, "data.getData() != null - Adding single Photo..");
                return;
            }
        }
  
        if (resultCode == RESULT_OK && requestCode == EDIT_LOCATION)
        {
            m_DR.nLongitude = data.getDoubleExtra(EditJournalLocation.DataLong, -1);
            m_DR.nLatitude = data.getDoubleExtra(EditJournalLocation.DataLat, -1);
        }
    }

    private void ProcessPhoto(Uri imageUri)
    {
        String sImageFile = getRealPathFromUri(imageUri);
        m_PhotoList.add(sImageFile);
        m_PhotoListAdapter.UpdateList(m_PhotoList);
        m_PhotoListAdapter.notifyDataSetChanged();

        //
        // If it is a new record the we will add it later
        if (m_Mode == ModeNewRecord)
            return;

        //
        // In edit mode, keep the list up to date.
        PhotoLinkTable TabPhotos = new PhotoLinkTable();
        TabPhotos.AddPhoto(m_Id, m_DR.Id, sImageFile);

        //
        // Add first photo to Master Table
        AddPhotoToMaster(sImageFile);
    }

    private void AddPhotoToMaster(String sPhoto)
    {
        if (m_sMainPhoto != null)
            return;

        TravelMasterTable Master = new TravelMasterTable();
        TravelMasterTable.DataRecord MDR = Master.QueryRecord(m_Id);
        if (MDR == null)
            return; // Shouldnt happen, but nothing I can do

        if (MDR.sPhoto != null && !MDR.sPhoto.isEmpty()) {
            m_sMainPhoto = MDR.sPhoto;
            return;
        }

        m_sMainPhoto = sPhoto;
        MDR.sPhoto = sPhoto;
        Master.UpdatePhoto(MDR.Id, sPhoto);
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column);
        cursor.close();
        return result;
    }

    private void CreateRecord() {
        m_DR.MasterId = m_Id;
        GetDataFromUI();

        long JournalId = m_Table.CreateTextNote(m_DR);

        //
        // Add Photos
        PhotoLinkTable TabPhotos = new PhotoLinkTable();
        for (int i = 0; i < m_PhotoList.size(); i++)
            TabPhotos.AddPhoto(m_Id, JournalId, m_PhotoList.get(i));

        //
        // Add the first one to Master
        if (m_PhotoList.size() > 0)
            AddPhotoToMaster(m_PhotoList.get(0));

        Prefs prefs = new Prefs(getApplicationContext());
        prefs.MarkJournalChange();
    }

    private void UpdateRecord() {
        GetDataFromUI();
        m_Table.UpdateRecord(m_DR);

        Prefs prefs = new Prefs(getApplicationContext());
        prefs.MarkJournalChange();
    }

    private void GetDataFromUI()
    {
        EditText sTitle = (EditText) findViewById(R.id.journal_title);
        EditText sBody = (EditText) findViewById(R.id.journal_entry);

        m_DR.sTitle = sTitle.getText().toString();
        m_DR.sStringNote = sBody.getText().toString();
        m_DR.nType = m_Spinner.getSelectedItemPosition();
        m_DR.bShowOnMap = m_ChkBoxShowOnMap.isChecked();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                PromptForDelete();
                break;
            case R.id.menu_edit_map:
                EditJournalLocation();
                break;

            default:
            return false;
        }
        return true;
    }

    private void EditJournalLocation()
    {
        Intent i = new Intent(JournalActivity.this, EditJournalLocation.class);
        i.putExtra(EditJournalLocation.DataType, m_DR.nType);
        i.putExtra(EditJournalLocation.DataLong, m_DR.nLongitude);
        i.putExtra(EditJournalLocation.DataLat, m_DR.nLatitude);
        startActivityForResult(i, EDIT_LOCATION);
    }

    private void PromptForDelete ()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.ru_sure_delete_journal);
        builder1.setCancelable(true);

        builder1.setNeutralButton(
                R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        DBManager DBM = DBManager.Get();
                        DBM.DeleteJournalEntry(m_DR.Id);
                        Toast.makeText(JournalActivity.this, R.string.journal_deleted,Toast.LENGTH_SHORT).show();
                        Prefs prefs = new Prefs(JournalActivity.this);
                        prefs.MarkJournalChange();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public class JournalTypeAdapter extends ArrayAdapter<String>
    {
        private final String[] m_Types;
        Context m_Ctx;

        public JournalTypeAdapter(Context context, int resource)
        {
            super(context, resource);
            m_Ctx = context;
            m_Types = getResources().getStringArray(R.array.event_types_array);
            addAll(m_Types);
        }
        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent)
        {

            LayoutInflater inflater = (LayoutInflater)m_Ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.journal_type, parent, false);

            TextView textView = (TextView) row.findViewById(R.id.spinnerText);
            textView.setText(m_Types[position]);

            ImageView imageView = (ImageView)row.findViewById(R.id.spinnerImage);
            imageView.setImageResource(Utils.GetIconList()[position]);

            return row;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
}