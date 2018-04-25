/*
 * Class : utils.TaskHelper
 * Author : iT Gurus Software
 * Copyright (C) 2017, iT Gurus Software. All rights reserved.
 */

package utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class TaskHelper
{
    public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task)
    {
        execute(task, (P[]) null);
    }

    @SuppressLint("NewApi")
    public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            Executor myExecutor = Executors.newFixedThreadPool(128);
            task.executeOnExecutor(myExecutor, params);
        } else
        {
            task.execute(params);
        }
    }
}
