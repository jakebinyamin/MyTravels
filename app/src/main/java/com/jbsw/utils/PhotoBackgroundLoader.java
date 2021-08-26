package com.jbsw.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.jbsw.mytravels.R;

import java.util.HashMap;
import java.util.Map;

public class PhotoBackgroundLoader
{
    private static final String TAG = "TAGPhotoBackground";
    private Map<ImageView, String> m_Map;
    private Bitmap m_BmpDefault = null;
    private int m_ResNotLoaded;
    private Handler m_Handler;
    private boolean m_bRoundedEdge = false;

    public PhotoBackgroundLoader()
    {
        m_Handler = new Handler();
        m_Map = new HashMap<ImageView, String>();
    }

    public void LoadPhoto(String sPhoto, ImageView vImg)
    {
        vImg.setImageResource(m_ResNotLoaded);
        m_Map.put(vImg, sPhoto);
        Log.d(TAG, "Size of Hash Map: " + m_Map.size());

        //
        // Start loading photo in the background
        PhotoLoader Loader = new PhotoLoader(vImg, sPhoto);
        Thread thread = new Thread(Loader);
        thread.start();
    }

    public void LoadDefaultPhoto(ImageView vImg)
    {
        vImg.setImageBitmap(m_BmpDefault);
    }
    public void SetRoundedEdge() { m_bRoundedEdge = true; }

    public void SetNotLoadedResource(int nRes)
    {
        m_ResNotLoaded = nRes;
    }

    public void LoadDefaultBitmap(Context context, int nRes)
    {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 4;
        m_BmpDefault = BitmapFactory.decodeResource(context.getResources(), nRes, bmOptions);
    }

    boolean ImageViewReused(ImageView v, String sPhoto)
    {
        //
        // Check if ImageView is used by something else now
        String sFileToLoad = m_Map.get(v);
        if(sFileToLoad==null || !sFileToLoad.equals(sPhoto)) {
            Log.d(TAG, "ImageView Reused: " + sFileToLoad);
            return true;
        }

        return false;
    }

    private class PhotoLoader implements Runnable {
        private final ImageView m_View;
        private final String m_sPhoto;

        PhotoLoader(ImageView v, String sPhoto) {
            m_View = v;
            m_sPhoto = sPhoto;
        }


        @Override
        public void run() {
            if (ImageViewReused(m_View, m_sPhoto))
                return;

            try {

                Bitmap myBitmap = Utils.LoadImage(m_sPhoto);

                //
                // Load the ImageView in the UI thread
                if (ImageViewReused(m_View, m_sPhoto))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(myBitmap, m_View, m_sPhoto);
                m_Handler.post(bd);

            } catch (Exception e) {
                Log.e(TAG, "Loading sPhoto: " + m_sPhoto + " " + e.getMessage());
            }
        }
    }

    class BitmapDisplayer implements Runnable
    {
        private final Bitmap m_Bitmap;
        private final ImageView m_View;
        String m_sPhoto;

        public BitmapDisplayer(Bitmap b, ImageView v, String sPhoto)
        {
            m_Bitmap=b;
            m_View = v;
            m_sPhoto = sPhoto;
        }
        public void run()
        {
            if (ImageViewReused(m_View, m_sPhoto))
                return;
            if (m_Bitmap == null) {
                Log.d(TAG, "m_Bitmap is null");
                return;
            }
            m_View.setImageBitmap(m_Bitmap);
            if (m_bRoundedEdge)
                m_View.setClipToOutline(true);
            String sFileToLoad = m_Map.get(m_View);
            if(sFileToLoad.equals(m_sPhoto))
                m_Map.remove(m_sPhoto, m_View);
        }
    }
}
