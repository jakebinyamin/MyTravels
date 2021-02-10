package com.jbsw.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.jbsw.data.NotesTable;
import com.jbsw.mytravels.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Utils
{
    static private final String TAG = "TAGUtils";
    static private Context m_Context;
    static private NotesTable.gpsCoordinateReceiver receiver;

    static public void SetContext(Context context)
    {
        m_Context = context;
    }

    static public Context GetContext()
    {
        return m_Context;
    }

    static public void RegisterGpsReceiver()
    {
        IntentFilter filter = new IntentFilter("com.jbw.MyTravels.LocData");
        receiver = new NotesTable.gpsCoordinateReceiver();
        m_Context.registerReceiver(receiver, filter);
    }

    static public void UnRegisterGpsReceiver()
    {
        m_Context.unregisterReceiver(receiver);
    }

    static public int[] GetIconList()
    {
        final int IconList[] = {R.drawable.pen, R.drawable.airplane, R.drawable.eat, R.drawable.relax, R.drawable.explore, R.drawable.hotel,
                R.drawable.shopping, R.drawable.bus, R.drawable.business, R.drawable.work, R.drawable.sport, R.drawable.excercise, R.drawable.hiking};
        return IconList;
    }

    static public int[] GetMapList()
    {
        final int IconList[] = {R.drawable.pin_pen, R.drawable.pin_plane, R.drawable.pin_eat, R.drawable.pin_relax, R.drawable.pin_explore, R.drawable.pin_hotel,
                R.drawable.pin_shopping, R.drawable.pin_travel, R.drawable.pin_business, R.drawable.pin_work, R.drawable.pin_sport, R.drawable.pin_excercise, R.drawable.pin_hiking
        };
        return IconList;
    }

    static public int[] GetColorList()
    {
        final int ClrList[] = { R.color.colorLine1, R.color.colorLine2, R.color.colorLine3, R.color.colorLine4, R.color.colorLine5, R.color.colorLine6 };
        return ClrList;
    }

    static public void SetRecordingAnimation(ImageView Img)
    {
        Img.setImageResource(R.drawable.recording);
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(500);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        Img.startAnimation(mAnimation);
    }

    public static Bitmap drawableToBitmap (Drawable drawable)
    {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    static public Bitmap LoadImage(String sPhoto)
    {
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(sPhoto, bmOptions);
            int scaleFactor = 4;

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            Bitmap myBitmap = BitmapFactory.decodeFile(sPhoto, bmOptions);

            ExifInterface exif = new ExifInterface(sPhoto);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            return myBitmap;
        }
        catch (Exception e) {
            Log.e(TAG, "Loading sPhoto: " + sPhoto + " " + e.getMessage());
            return null;
        }
    }

    static public long CalculateDays(String sFrom, String sTo)
    {
        if (sFrom == null || sTo == null)
            return -1;
        LocalDate dateFrom = GetDateFromString(sFrom);
        LocalDate dateTo = GetDateFromString(sTo);
        if (dateFrom == null || dateTo == null)
            return -1;

        long nDiff = ChronoUnit.DAYS.between(dateFrom, dateTo);
        return nDiff + 1;
    }

    static public String GetDateWithNoTime(String sDate)
    {
        // Format we are aiming for yyyy-MM-dd"
        int Idx = sDate.indexOf(' ');
        if (Idx < 0 && sDate.length() == 10)
            return sDate;
        if (Idx < 0)
            return null;
        String sNew = sDate.substring(0, Idx);
        return sNew;
    }

    static public LocalDate GetDateFromString(String sDate)
    {
        String sCorrectedDate = GetDateWithNoTime(sDate);
        if (sCorrectedDate == null)
            return null;
        LocalDate Date = null;
        try {
            Date = LocalDate.parse(sCorrectedDate);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return Date;
    }

    static public String GetDatePlusDays(String sDate, long nPlusDays)
    {
        LocalDate dateCurr = GetDateFromString(sDate);
        if (dateCurr == null)
            return null;

        LocalDate datePlusDays = dateCurr.plusDays(nPlusDays);

        String sOut = String.format("%d-%02d-%02d", datePlusDays.getYear(), datePlusDays.getMonthValue(), datePlusDays.getDayOfMonth());
        return sOut;
    }

    static public String GetTimeFromString(String sDate)
    {
        String sCorrectedString = sDate.replace(' ', 'T');
        LocalDateTime dt = null;
        try {
            dt = LocalDateTime.parse(sCorrectedString);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "";
        }

        String sOut = String.format("%d:%02d", dt.getHour(), dt.getMinute());
        return sOut;
    }

    static public String GetReadableStringDate(String sDate)
    {
        if (sDate == null)
            return "";

        LocalDate date = GetDateFromString(sDate);
        if (date == null)
            return "";
        DayOfWeek nDow = date.getDayOfWeek();
        String sDow = nDow.getDisplayName(TextStyle.SHORT, Locale.getDefault());;
        Month nMonth = date.getMonth();
        String sMonth = nMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault());

        String sOut = String.format("%s %s %d, %d", sDow, sMonth, date.getDayOfMonth(), date.getYear());
        return sOut;
    }

    static public String GetReadableStringDateWithTime(String sDate)
    {
        String sTime = GetTimeFromString(sDate);
        String sDateOnly = GetReadableStringDate(sDate);

        String sOut = String.format("%s, %s", sTime, sDateOnly);
        return sOut;
    }

}
