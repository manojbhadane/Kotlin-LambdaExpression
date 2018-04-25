package com.springtech.fragment.base;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.springtech.R;
import com.springtech.app.App;
import com.springtech.utils.Utils;

/**
 * Created by manoj.bhadane on 18-01-2018.
 */
public abstract class BaseFragment<B extends ViewDataBinding> extends Fragment
{
    private B mDataBinding;
    Dialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mDataBinding = DataBindingUtil.inflate(inflater, getLayoutResId(), container, false);
        Utils.setFont((ViewGroup) mDataBinding.getRoot().findViewById(R.id.parent), App.getRegularFont());
        init(mDataBinding);
        return mDataBinding.getRoot();
    }

    public abstract int getLayoutResId();

    public abstract void init(B binding);

    public void showProgress()
    {
        setDialog();
    }

    private void setDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.layout_progress);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), android.R.color.transparent)));
        dialog.show();
    }


    public void hideProgress()
    {
        if (dialog!=null)
        {
            if (dialog.isShowing())
            {
                ///dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                dialog.dismiss();
            }
        }
    }

}
