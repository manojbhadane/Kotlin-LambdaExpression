package com.springtech.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.springtech.R;
import com.springtech.app.App;

/**
 * Created by manoj.bhadane on 19-01-2018.
 */
public class CustomTextInputLayout extends RelativeLayout
{
    private LayoutInflater mInflater;
    private TextInputLayout mTxtInputLay;
    private EditText mEdtText;
    private Context mContext;
    private View mTransparentView;

    public EditText getmEdtText()
    {
        return mEdtText;
    }


    public TextInputLayout getmTxtInputLay()
    {
        return mTxtInputLay;
    }

    public CustomTextInputLayout(Context context)
    {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        init("Label", "Hint", "Value", false, false, 10, 1, false);
    }

    public CustomTextInputLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextInputLayout);
        String labelName = a.getString(R.styleable.CustomTextInputLayout_cTIPlLabelName);
        String edtHint = a.getString(R.styleable.CustomTextInputLayout_cTIPlEdtHint);
        String edtText = a.getString(R.styleable.CustomTextInputLayout_cTIPlEdtText);
        boolean isTypeInt = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeInt, false);
        boolean isTypeFloat = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeFloat, false);
        int maxInt = a.getInt(R.styleable.CustomTextInputLayout_cTIPlIntMaxLength, 10);
        int maxLines = a.getInt(R.styleable.CustomTextInputLayout_cTIPMaxLines, 1);
        boolean isNameField = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeName, false);


        mInflater = LayoutInflater.from(context);
        init(labelName, edtHint, edtText, isTypeInt, isTypeFloat, maxInt, maxLines, isNameField);
    }

    public CustomTextInputLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextInputLayout);
        String labelName = a.getString(R.styleable.CustomTextInputLayout_cTIPlLabelName);
        String edtHint = a.getString(R.styleable.CustomTextInputLayout_cTIPlEdtHint);
        String edtText = a.getString(R.styleable.CustomTextInputLayout_cTIPlEdtText);
        boolean isTypeInt = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeInt, false);
        boolean isTypeFloat = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeFloat, false);
        int maxInt = a.getInt(R.styleable.CustomTextInputLayout_cTIPlIntMaxLength, 10);
        int maxLines = a.getInt(R.styleable.CustomTextInputLayout_cTIPMaxLines, 1);
        boolean isNameField = a.getBoolean(R.styleable.CustomTextInputLayout_cTIPlTypeName, false);

        mInflater = LayoutInflater.from(context);
        init(labelName, edtHint, edtText, isTypeInt, isTypeFloat, maxInt, maxLines, isNameField);
    }

    public void init(String labelName, String edtHint, String edtText, boolean isTypeInt, boolean isTypeFloat, int maxInt, int maxLines, boolean isNameField)
    {
        View v = mInflater.inflate(R.layout.custom_textinputlay, this, true);
        mTxtInputLay = (TextInputLayout) v.findViewById(R.id.customTextInputLay);
        mTransparentView = v.findViewById(R.id.view);
        mEdtText = (EditText) v.findViewById(R.id.edtText);
        mTxtInputLay.setHint(edtHint);
        mEdtText.setText(edtText);
        mEdtText.setTypeface(App.getRegularFont());

        Utils.showLog(String.valueOf(maxLines));

        if (isTypeInt)
        {
            mEdtText.setInputType(InputType.TYPE_CLASS_NUMBER);
            mEdtText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxInt)});
        } else if (isTypeFloat)
        {
            mEdtText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            mEdtText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxInt)});
        }else
        if (isNameField)
        {
            mEdtText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mEdtText.setFilters(new InputFilter[]{Utils.getCharacterFilter(),new InputFilter.LengthFilter(50)});
        } else
        {
            mEdtText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }


        if (maxLines > 1)
        {
            mEdtText.setSingleLine(false);
            mEdtText.setMaxLines(maxLines);
        } else
        {
            mEdtText.setSingleLine(true);
        }
    }

    public void setHint(String hint)
    {
        mEdtText.setHint(hint);
    }

    public void setEdtText(String text)
    {
        mEdtText.setText(text);
    }


    public View getmTransparentView()
    {
        return mTransparentView;
    }

    public String getEdtValue()
    {
        return mEdtText.getText().toString();
    }


    public void addDrawableRight(int id)
    {
        Drawable image = mContext.getResources().getDrawable(id);
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds(0, 0, w, h);
        mEdtText.setCompoundDrawablePadding(15);
        mEdtText.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
    }

    public void disableTextInputLayEditing()
    {
        mTxtInputLay.setFocusable(true);
        mEdtText.setFocusable(false);
        getmTransparentView().setVisibility(View.VISIBLE);
    }
}
