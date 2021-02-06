package com.jbsw.mytravels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jbsw.utils.PhotoBackgroundLoader;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoListViewAdapter extends RecyclerView.Adapter<PhotoListViewAdapter.PhotoViewHolder>
{
    private static final String TAG = "PhotoListViewAdapter";
    private ArrayList<String> m_PhotoList = null;
    private PhotoItemClickListener m_ClickListener = null;
    private PhotoBackgroundLoader m_BkgLoader;

    public PhotoListViewAdapter(ArrayList<String> List)
    {
        UpdateList(List);
        m_BkgLoader = new PhotoBackgroundLoader();
        m_BkgLoader.SetNotLoadedResource(R.drawable.photo_loading_bkg);
        m_BkgLoader.SetRoundedEdge();
    }

    public void UpdateList(ArrayList<String> List)
    {
        m_PhotoList = List;
    }

    @NonNull
    @Override
    public PhotoListViewAdapter.PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_listview_item, parent, false);

        return new PhotoListViewAdapter.PhotoViewHolder(view);
    }

    // allows clicks events to be caught
    public void setPhotoClickListener(PhotoListViewAdapter.PhotoItemClickListener itemClickListener)
    {
        m_ClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface PhotoItemClickListener {
        void onPhotoItemClick(View view, int position);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoListViewAdapter.PhotoViewHolder holder, int position)
    {
        ImageView View = holder.GetImageView();
        m_BkgLoader.LoadPhoto(m_PhotoList.get(position), View);
//        try {
//            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            bmOptions.inJustDecodeBounds = false;
//            bmOptions.inSampleSize = 4;
//            Bitmap bm = null;
//            bm = BitmapFactory.decodeFile(m_PhotoList.get(position), bmOptions);
//            if (bm == null) {
//                Log.e(TAG, "URL cant convert to bmp!! - " + m_PhotoList.get(position));
//                return;
//            }
//            View.setImageBitmap(bm);
//            View.setClipToOutline(true);
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
    }

    @Override
    public int getItemCount()
    {
        return m_PhotoList.size();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final ImageView m_View;

        public PhotoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            //m_View = itemView.findViewById(R.id.image_cell);
            m_View = (ImageView) itemView;
            m_View.setOnClickListener(this);
        }

        public ImageView GetImageView()
        {
            return m_View;
        }

        @Override
        public void onClick(View v)
        {
            if (m_ClickListener != null)
                m_ClickListener.onPhotoItemClick(v, getAdapterPosition());
        }
    }
}