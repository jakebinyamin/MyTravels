package com.jbsw.mytravels;


import com.jbsw.GoogleDrive.RestoreData;

public class RestoreProgress extends BaseBackupRestoreProgress
{
    public RestoreProgress()
    {
        m_Mode = Mode.MODE_RESTORE;
    }

    @Override
    protected void Start()
    {
        RestoreData RD = new RestoreData(this, m_GoogleAccount);
        RD.SetCallBack(this);
        Thread ThrdBkp = new Thread(RD);
        ThrdBkp.start();
    }
}