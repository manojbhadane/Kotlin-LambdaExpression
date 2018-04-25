package com.springtech.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.springtech.R;
import com.springtech.app.App;

/**
 * Created by manoj.bhadane on 26-02-2018.
 */
public class CustomDialogWithText extends Dialog
{
    Context mContext;

    public CustomDialogWithText(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(false);
        setContentView(R.layout.progressbarwithtext);
        TextView textView = (TextView) findViewById(R.id.pro_txt);
        textView.setTypeface(App.getBoldFont());
    }
}

