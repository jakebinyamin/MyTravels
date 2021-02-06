package com.jbsw.mytravels;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jbsw.data.TravelMasterTable;

public class CreateProject extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "TagCreateProject";
    private Button m_BtnCancel,m_BtnCreate;
    private View m_BtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        m_BtnCreate = (Button)findViewById(R.id.do_create);
        if (m_BtnCreate != null)
            m_BtnCreate.setOnClickListener(this);
        m_BtnCancel = (Button)findViewById(R.id.cancel);
        if (m_BtnCancel != null)
            m_BtnCancel.setOnClickListener(this);
        m_BtnBack = findViewById(R.id.back);
        if (m_BtnBack != null)
            m_BtnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnCancel)
            finish();

        if (v == m_BtnBack) {
            PromptDialog();
            return;
        }

        if (v == m_BtnCreate)
        {
            SaveAndExit();
        }
    }

    private void SaveAndExit()
    {
        EditText edName = (EditText)findViewById(R.id.trip_name);
        if (edName.getText().toString().isEmpty())
        {
            PromptError();
            return;
        }

        long Res = CreateRecord();
        if (Res != -1)
        {
            Intent intent = new Intent(this, TripActivity.class);
            intent.putExtra("DATARECORD", Res);
            startActivity(intent);
        }

        finish();
    }

    private void PromptError()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.error_no_title);
        builder1.setCancelable(true);

        builder1.setNeutralButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void PromptDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.exit_no_save);
        builder1.setCancelable(true);

        builder1.setNeutralButton(
                R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        SaveAndExit();
                    }
                });

        builder1.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private long CreateRecord()
    {
        EditText edName = (EditText)findViewById(R.id.trip_name);
        EditText edDescr = (EditText)findViewById(R.id.TripDescription);
        long Res = TravelMasterTable.CreateRecord(edName.getText().toString(), edDescr.getText().toString(), true);
        if (Res == -1)
            Toast.makeText(this, "Failed to Write to DB", Toast.LENGTH_SHORT).show();
        return Res;
    }
}
