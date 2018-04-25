/*
 * Class : utils.Utils
 * Author : iT Gurus Software
 * Copyright (C) 2017, iT Gurus Software. All rights reserved.
 */

package utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import customViews.IconView;
import innomationtech.doctoplus.doctor.BuildConfig;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Utils
{
    /**
     * function to hide softkeyboard when click anywhere except edittext on screen
     *
     * @param lActivity
     * @param view
     * @param lEditText
     * @param lMagGlass
     */
    public static void setupUI(final Activity lActivity, View view, final EditText lEditText, final ImageView lMagGlass)
    {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText))
        {
            view.setOnTouchListener(new OnTouchListener()
            {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1)
                {
                    onHideKeyBoard(lActivity, lEditText, lMagGlass);
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(lActivity, innerView, lEditText, lMagGlass);
            }
        }
    }

    public static void onHideKeyBoard(Activity mActivity, EditText lEditText, ImageView lMagGlass)
    {
        final InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lEditText.getWindowToken(), 0);

        lEditText.clearFocus();
        if (lMagGlass != null)
        {
            if (TextUtils.isEmpty(lEditText.getText().toString()))
            {
                lMagGlass.setVisibility(View.VISIBLE);
            } else
            {
                lMagGlass.setVisibility(View.GONE);
            }
        }
    }

    public void hideSoftKeyBoardOnTabClicked(Context context, View v)
    {
        if (v != null && context != null)
        {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideKeyboard(@NonNull Activity activity)
    {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null)
        {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // to animate expand and collapse
    public static Animation expand(final View v, final boolean expand)
    {
        try
        {
            Method m = v.getClass().getDeclaredMethod("onMeasure", int.class, int.class);
            m.setAccessible(true);
            m.invoke(v, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(
                    ((View) v.getParent()).getMeasuredWidth(), MeasureSpec.AT_MOST));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        final int initialHeight = v.getMeasuredHeight();

        if (expand)
        {
            v.getLayoutParams().height = 0;
        } else
        {
            v.getLayoutParams().height = initialHeight;
        }
        v.setVisibility(View.VISIBLE);

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {
                int newHeight = 0;
                if (expand)
                {
                    newHeight = (int) (initialHeight * interpolatedTime);
                } else
                {
                    newHeight = (int) (initialHeight * (1 - interpolatedTime));
                }
                v.getLayoutParams().height = newHeight;
                v.requestLayout();

                if (interpolatedTime == 1 && !expand)
                    v.setVisibility(View.GONE);
            }

            @Override
            public boolean willChangeBounds()
            {
                return true;
            }
        };

        a.setDuration(400);
        a.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                // animWorkingFlag=false;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationStart(Animation animation)
            {
                // animWorkingFlag=true;
            }
        });

        return a;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Display toast message
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Print Logs
     *
     * @param msg
     */
    public static void showLog(String msg)
    {
        if (BuildConfig.DEBUG)
            Log.e("TAG_Error", msg);
    }

    public static void showLog(Context context, String msg)
    {
        if (BuildConfig.DEBUG)
            Log.e("--" + context.getClass().getName().toString() + "--", msg);
    }

    public static String dateFormat(String input)
    {

        String lStr = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try
        {
            Date inputDate = inputFormat.parse(input);
            lStr = outputFormat.format(inputDate);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return lStr;
    }

    /**
     * set Font to entire screen at once
     *
     * @param group     : parent layout id
     * @param lTypeface : font
     */
    public static void setFont(ViewGroup group, Typeface lTypeface)
    {
        // TODO function to set font for entire layout
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++)
        {
            v = group.getChildAt(i);
            if (v instanceof IconView)
            {
//                ((TextView) v).setTypeface(lTypeface);
            } else if (v instanceof TextView)
            {
                ((TextView) v).setTypeface(lTypeface);
            } else if (v instanceof EditText)
            {
                ((EditText) v).setTypeface(lTypeface);
            } else if (v instanceof Button)
            {
                ((Button) v).setTypeface(lTypeface);
            } else if (v instanceof TextInputLayout)
            {
                ((TextInputLayout) v).setTypeface(lTypeface);
            }
            if (v instanceof ViewGroup)
                setFont((ViewGroup) v, lTypeface);
        }
    }

    /**
     * function for adding 0. E.g. Time: 4:7 , 04:07
     *
     * @param input
     * @return
     */
    public String pad(int input)
    {
        String str = "";
        if (input >= 10)
        {
            str = Integer.toString(input);
        } else
        {
            str = "0" + Integer.toString(input);
        }
        return str;
    }

    /**
     * function to convert 24 hours to 12 hours
     *
     * @param time
     * @return
     */
    public String Convert24to12(String time)
    {
        String convertedTime = "";
        try
        {
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm", Locale.US);
            Date date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
//            convertedTime.replace("AM", "am").replace("PM","pm");
//            System.out.println("convertedTime : " + convertedTime);
        } catch (final ParseException e)
        {
            e.printStackTrace();
        }
        return convertedTime;
        // Output will be 10:23 PM
    }

    /**
     * function to convert 12 hours to 24 hours
     *
     * @param time
     * @return
     */
    public String Convert12to24(String time)
    {
        String convertedTime = "";
        try
        {
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
//            convertedTime.replace("AM", "am").replace("PM","pm");
//            System.out.println("convertedTime : " + convertedTime);
        } catch (final ParseException e)
        {
            e.printStackTrace();
        }
        return convertedTime;
        // Output will be 10:23 PM
    }

    @SuppressLint("SimpleDateFormat")
    public String getFormatedDate()
    {
        String dayNumberSuffix = getDayNumberSuffix(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        SimpleDateFormat dateFormat = new SimpleDateFormat(" d'" + dayNumberSuffix + "' MMM yyyy", Locale.US);
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    private String getDayNumberSuffix(int day)
    {
        if (day >= 11 && day <= 13)
        {
            return "<sup>th</sup>";
        }
        switch (day % 10)
        {
            case 1:
                return "<sup>st</sup>";
            case 2:
                return "<sup>nd</sup>";
            case 3:
                return "<sup>rd</sup>";
            default:
                return "<sup>th</sup>";
        }
    }

    /**
     * change date format from any format to any format
     *
     * @param inputDateString
     * @param inputFormat
     * @param outputFormat
     * @return
     */
    public static String changeDateFormat(String inputDateString, String inputFormat, String outputFormat)
    {
//        SimpleDateFormat dateFormat = new SimpleDateFormat(
//                "yyyy-MM-dd", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                inputFormat, Locale.US);

        Date myDate = null;
        try
        {
            myDate = dateFormat.parse(inputDateString);

        } catch (ParseException e)
        {
            e.printStackTrace();
        }

//        SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat(outputFormat, Locale.US);
        String finalDate = timeFormat.format(myDate);
        return finalDate;
    }

    public String getDayOfWeek(String date, boolean isShortForm)
    {
        SimpleDateFormat sdf;
        if (isShortForm)
        {
            //e.g. Sun
            sdf = new SimpleDateFormat("EE");
        } else
        {
            //e.g. Sunday
            sdf = new SimpleDateFormat("EEEE");
        }

        Date d = new Date(date);
        String dayOfTheWeek = sdf.format(d);
        return dayOfTheWeek;
    }

    public Bitmap roundCornerImage(Bitmap raw, float round)
    {
        int width = raw.getWidth();
        int height = raw.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawARGB(0, 0, 0, 0);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#000000"));

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        canvas.drawRoundRect(rectF, round, round, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(raw, rect, rect, paint);

        return result;
    }

    public int ConvertTOMinuts(String Date)
    {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
        Date date = null;
        try
        {
            date = df.parse(Date);

        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        int minuts = date.getHours() * 60 + date.getMinutes();

        return minuts;
    }

    public int getMinuts()
    {
        Date date = getISTTime();
        int minuts = date.getHours() * 60 + date.getMinutes();
//        Log.e("Utils minitus = ", "" + minuts);
        return minuts;
    }

    public Date getISTTime()
    {
        final SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        final String utcTime = df.format(new Date());
//        Log.e("Utils Time = ", utcTime);
        Date date = null;
        try
        {
            date = df.parse(utcTime);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }

    public String getISTDataTime()
    {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        final String utcTime = df.format(new Date());
//        Log.e("Utils Time = ", utcTime);
//        Date date = null;
//        try
//        {
//            date = df.parse(utcTime);
//        } catch (ParseException e)
//        {
//            e.printStackTrace();
//        }
        return utcTime;
    }

    public String getUTCTime()
    {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;

//        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        // df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
//        // sdf.setTimeZone(TimeZone.getDefault());
//        df.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        final String utcTime = df.format(new Date());
//
//       // df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
//
//        final String utcTimeIST = df.format(new Date());
//        Log.e("Utils getUTCTime = ", utcTime + " IST " + utcTimeIST);
//        getMinuts();
//
//
//        Date date = null;
//        try
//        {
//            date = df.parse(utcTime);
//        } catch (ParseException e)
//        {
//            e.printStackTrace();
//        }
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        Log.e("B4chnagegetUTCTime= ", "" + cal.get(Calendar.HOUR_OF_DAY) + " : " + cal.get(Calendar.MINUTE));
////Set the time for the notification to occur.
//        cal.add(Calendar.HOUR_OF_DAY, 5); // adds one hour
//        cal.add(Calendar.MINUTE, 30); // adds one Minute
//        cal.getTime(); // returns new
//
//        Log.e("afterchnagegetUTCTime= ", "" + cal.get(Calendar.HOUR_OF_DAY) + " : " + cal.get(Calendar.MINUTE));
//        return utcTime;
    }

    public int getCount(EditText editText)
    {
        String StrCount = editText.getText().toString().trim();
        int patientCount = StrCount.equals("") ? 0 : Integer.valueOf(StrCount);
        return patientCount;
    }

    public float getUnitCount(EditText editText)
    {
        String StrCount = editText.getText().toString().trim();
        float patientCount = StrCount.equals("") ? 0 : Float.valueOf(StrCount);
        return patientCount;
    }

    public static double roundToHalf(double d)
    {
        return Math.round(d * 2) / 2.0;
    }

    public static Date stringToDate(String date, String dateFormat)
    {
//        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
        try
        {
            Date date1 = format.parse(date);
            return date1;
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//        String strDate = "Current Date : " + mdformat.format(calendar.getTime());
        return mdformat.format(calendar.getTime());
    }

    /**
     * function to resize bitmap images
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public Bitmap BITMAP_RESIZER(Bitmap bitmap, int newWidth, int newHeight)
    {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        /*Log.v("Pictures", "Width and height are " + width + "--" + height);*/

        if (width > height)
        {
            // landscape
            float ratio = (float) width / newWidth;
            width = newWidth;
            height = (int) (height / ratio);
        } else if (height > width)
        {
            // portrait
            float ratio = (float) height / newHeight;
            height = newHeight;
            width = (int) (width / ratio);
        } else
        {
            // square
            height = newHeight;
            width = newWidth;
        }

        Bitmap scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float ratioX = width / (float) bitmap.getWidth();
        float ratioY = height / (float) bitmap.getHeight();
      /* float ratioX = newWidth / (float) bitmap.getWidth();
       float ratioY = newHeight / (float) bitmap.getHeight();*/
        float middleX = width / 2.0f;
        float middleY = height / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        //canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }

    /**
     * Showing the status in Snackbar
     *
     * @param context
     * @param message
     */
    public static void showSnack(Context context, String message)
    {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);

        Snackbar snackbar = Snackbar
                .make(rootView, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
