package com.springtech.activity.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.springtech.R;
import com.springtech.app.App;
import com.springtech.utils.LanguageContextWraper;
import com.springtech.utils.Utils;

/**
 * Created by manoj.bhadane on 18-01-2018.
 */
public abstract class BaseActivity<B extends ViewDataBinding> extends AppCompatActivity implements View.OnTouchListener
{
    private B mDataBinding;
    public Toolbar mToolbar;
    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        App.CurrentContext = this;
        mDataBinding = DataBindingUtil.setContentView(this, getLayoutResId());
        setToolbar();
        Utils.setFont((ViewGroup) findViewById(R.id.parent), App.getRegularFont());
        init(mDataBinding);
        getWindow().getAttributes().windowAnimations = R.style.Fade;

    }

    public abstract int getLayoutResId();

    public abstract void init(B dataBinding);

    public void setToolbar()
    {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    public Toolbar getToolbar()
    {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    public void hideToolbar()
    {
        mToolbar.setVisibility(View.GONE);
    }

    public void showToolbarBackBtn(boolean isShown)
    {
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(isShown);
            getSupportActionBar().setDisplayShowHomeEnabled(isShown);
        }
    }

    public void setToolbarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    protected void gotoActivity(Class<?> cls)
    {
        gotoActivity(cls, null);
    }

    protected void gotoActivity(Class<?> cls, Bundle bundle)
    {
        Intent intent = new Intent(this, cls);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivity(intent);
    }

    protected void gotoActivityThenKill(Class<?> cls, Bundle bundle)
    {
        gotoActivity(cls, bundle);
        finish();
    }

    /**
     * Default back button click to go on prev screen
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(LanguageContextWraper.wrap(newBase, App.getDefaultLocal()));
    }


    public void showProgress(boolean isCancelable)
    {
        setDialog(isCancelable);
    }

    private void setDialog(boolean isCancelable)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.layout_progress);
        dialog = builder.create();
        if (!isCancelable)
        {
            dialog.setCanceledOnTouchOutside(false);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }


    public void hideProgress()
    {
        if (dialog != null)
        {
            if (dialog.isShowing())
            {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                dialog.dismiss();
            }
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        hideProgress();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (v.getId())
        {
            case R.id.toolbar:
                Utils.hideSoftKeyboard(this);
                break;
        }
        return false;
    }
}
