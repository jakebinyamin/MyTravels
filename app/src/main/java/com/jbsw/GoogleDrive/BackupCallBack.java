package com.jbsw.GoogleDrive;

public interface BackupCallBack
{
    public void SetMessage(int id);
    public void setBackupCount(int nCnt);
    public void IncrementProgress();
    public void Complete();
}
