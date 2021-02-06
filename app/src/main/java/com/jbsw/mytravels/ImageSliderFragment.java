package com.jbsw.mytravels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jbsw.utils.Utils;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ImageSliderFragment extends Fragment
{
    private static final String TAG = "TAGImageSliderFragment";

    private static final String IMAGE_URL = "image_url";

    public static ImageSliderFragment newInstance(String imageUrl) {
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageUrl);
        ImageSliderFragment fragment = new ImageSliderFragment();
        Log.d(TAG, "file path received: " + imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "In OnCreateView of ImageSliderFragment");
        ImageView view = null;
        try {
            view = (ImageView) inflater.inflate(R.layout.item_slider, container, false);
            String url = getUrlFromInstance();
            Log.d(TAG, "file path in onCreateView: " + url);
            loadImage(view, url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return view;
    }

    private void loadImage(ImageView view, String url) {
        try {
//            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            bmOptions.inJustDecodeBounds = false;
//            bmOptions.inSampleSize = 4;
//            Bitmap bm = null;
//            bm = BitmapFactory.decodeFile(url, bmOptions);
//            if (bm == null) {
//                    Log.e(TAG, "URL cant convert to bmp!! " + url);
//                    return;
//            }
//            view.setImageBitmap(bm);

            Bitmap bm = Utils.LoadImage(url);
            if (bm != null)
                view.setImageBitmap(bm);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private String getUrlFromInstance()
    {
        return getArguments().getString(IMAGE_URL);
    }
}
