package com.springtech.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.springtech.app.App;
import com.springtech.requestmodel.AddPriceDetail;
import com.springtech.responsemodel.AdvertisementWrapper;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by manoj.bhadane on 18-01-2018.
 */
public class SharedPrefrancesValue
{
    public static SharedPrefrancesValue mInstance;

    private final String PREF_MobileNo = "MobileNo";
    private final String PREF_UserType = "UserType";
    private final String PREF_OTP = "OTP";
    private final String PREF_UserDetailsId = "UserDetailsId";
    private final String FILTER_OBJECT = "filter_object";
    private final String ADVERTISEMENTLIST_OBJECT = "advertisement_object";
    private final String ADVERTISEMENT_OBJECT = "advertisement_id";
    private String ADD_PRICE_OBJECT = "add_price_object";

    private final String PREF_Farmer_LastCheckedDtm = "Farmer_LastCheckedDtm";
    private final String PREF_Farmer_TodaysRateDate = "Farmer_TodaysRateDate";
    private final String PREF_Farmer_Price = "Farmer_Price";
    private final String PREF_Buyers_LastCheckedDtm = "Buyers_LastCheckedDtm";
    private final String PREF_AllCrop_LastCheckedDtm = "AllCrop_LastCheckedDtm";
    private final String PREF_Advertisement_LastCheckedDtm = "Advertisement_LastCheckedDtm";
    private final String PREF_IS_LOGIN = "Is_login";

    private SharedPrefrancesValue()
    {
    }

    public static synchronized SharedPrefrancesValue getInstance()
    {
        if (mInstance == null)
            mInstance = new SharedPrefrancesValue();
        return mInstance;
    }

    public SharedPreferences getPrefs()
    {
        return App.getInstance().getSharedPreferences("UserNameAcrossApplication", App.getInstance().MODE_PRIVATE | App.getInstance().MODE_MULTI_PROCESS);
    }

    public String getMobileNo()
    {
        return getPrefs().getString(PREF_MobileNo, "default");
    }

    public void setMobileNo(String value)
    {
        getPrefs().edit().putString(PREF_MobileNo, value).commit();
    }

    public int getUserType()
    {
        return getPrefs().getInt(PREF_UserType, 0);
    }

    public void setUserType(int value)
    {
        getPrefs().edit().putInt(PREF_UserType, value).commit();
    }


    public void putString(String key, String value)
    {
        getPrefs().edit().putString(key, value).commit();
    }

    public String getString(String key, String value)
    {
        return getPrefs().getString(key, value);
    }

    public void putBoolean(String key, boolean value)
    {
        getPrefs().edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key, boolean value)
    {
        return getPrefs().getBoolean(key, value);
    }

    public void putInt(String key, int value)
    {
        getPrefs().edit().putInt(key, value).commit();
    }

    public int getInt(String key)
    {
        return getPrefs().getInt(key, 0);
    }


    public String getOTP()
    {
        return getPrefs().getString(PREF_OTP, null);
    }

    public void setOTP(String OTP)
    {
        getPrefs().edit().putString(PREF_OTP, OTP).commit();
    }

    public String getUserDetailsId()
    {
        return getPrefs().getString(PREF_UserDetailsId, null);
    }

    public void setUserDetailsId(String UserDetailsId)
    {
        getPrefs().edit().putString(PREF_UserDetailsId, UserDetailsId).commit();
    }

    public void setFilterObject(String value)
    {
        getPrefs().edit().putString(FILTER_OBJECT, value).commit();
    }

    public String getFilterObject()
    {
        return getPrefs().getString(FILTER_OBJECT, null);
    }

    public void putAdvertisement(ArrayList<AdvertisementWrapper> advertisementlist)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS", Locale.US);
        Date startDate;
        Date endDate;

        for (int i = 0; i < advertisementlist.size(); i++)
        {
            try
            {
                startDate = simpleDateFormat.parse(advertisementlist.get(i).getDisplayStartDtm());
                endDate = simpleDateFormat.parse(advertisementlist.get(i).getDisplayEndDtm());
                Date currentDate = new Date();

                if ((currentDate.after(startDate) && currentDate.before(endDate)) || (currentDate.compareTo(startDate) == 0 || currentDate.compareTo(endDate) == 0))
                {
                } else
                {
                    advertisementlist.remove(i);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Gson gson = new Gson();
        getPrefs().edit().putString(ADVERTISEMENTLIST_OBJECT, gson.toJson(advertisementlist)).commit();
    }

    public List<AdvertisementWrapper> getAdvertisement()
    {
        String jsonString = getPrefs().getString(ADVERTISEMENTLIST_OBJECT, null);
        if (jsonString != null)
        {
            Type type = new TypeToken<List<AdvertisementWrapper>>()
            {
            }.getType();
            Gson gson = new Gson();
            List<AdvertisementWrapper> advertisementWrapperList = gson.fromJson(jsonString, type);
            Collections.sort(advertisementWrapperList, AdvertisementWrapper.DisplayOrderComparator);

            Iterator<AdvertisementWrapper> itr = advertisementWrapperList.iterator();
            while (itr.hasNext())
            {
                if (itr.next().getUserType() != getUserType())
                {
                    itr.remove();
                }
            }
            Utils.showLog("------Size------- " + advertisementWrapperList.size());
            return advertisementWrapperList.size() == 0 ? null : advertisementWrapperList;

        } else
        {
            return null;
        }
    }

    public String getFarmerLastCheckDtm()
    {
        return getPrefs().getString(PREF_Farmer_LastCheckedDtm, "2018-01-01 00:00:00");
    }

    public void setFarmerLastCheckDtm(String value)
    {
        getPrefs().edit().putString(PREF_Farmer_LastCheckedDtm, value).commit();
    }

    public String getFarmerTodaysRateDtm()
    {
        return getPrefs().getString(PREF_Farmer_TodaysRateDate, null);
    }

    public void setFarmerTodaysRateDtm(String value)
    {
        getPrefs().edit().putString(PREF_Farmer_TodaysRateDate, value).commit();
    }

    public String getFarmerPrice()
    {
        return getPrefs().getString(PREF_Farmer_Price, "0");
    }

    public void setFarmerPrice(String value)
    {
        getPrefs().edit().putString(PREF_Farmer_Price, value).commit();
    }


    public AddPriceDetail getPriceObject()
    {
        AddPriceDetail addPriceDetail = null;
        Gson gson = new Gson();
        if (getPrefs().getString(ADD_PRICE_OBJECT, null) != null)
        {
            addPriceDetail = gson.fromJson(getPrefs().getString(ADD_PRICE_OBJECT, null), AddPriceDetail.class);
        }
        return addPriceDetail;
    }

    public void putPriceObject(AddPriceDetail addPriceModel)
    {
        Gson gson = new Gson();
        getPrefs().edit().putString(ADD_PRICE_OBJECT, gson.toJson(addPriceModel)).commit();
    }

    public void setPREF_Buyers_LastCheckedDtm(String value)
    {
        getPrefs().edit().putString(PREF_Buyers_LastCheckedDtm, value).commit();
    }

    public String getPREF_Buyers_LastCheckedDtm()
    {
        return getPrefs().getString(PREF_Buyers_LastCheckedDtm, "2018-01-01 00:00:00");
    }

    public void setAllCropLastCheckedDtm(String value)
    {
        getPrefs().edit().putString(PREF_AllCrop_LastCheckedDtm, value).commit();
    }

    public String getAllCropLastCheckedDtm()
    {
        return getPrefs().getString(PREF_AllCrop_LastCheckedDtm, "2018-01-01 00:00:00");
    }


    public String getAdvertisementLastCheckDtm()
    {
        return getPrefs().getString(PREF_Advertisement_LastCheckedDtm, "2018-01-01 00:00:00");
    }

    public void setAdvertisementLastCheckDtm(String value)
    {
        getPrefs().edit().putString(PREF_Advertisement_LastCheckedDtm, value).commit();
    }

    public boolean getIsLogin()
    {
        return getPrefs().getBoolean(PREF_IS_LOGIN, false);
    }

    public void setIsLogin(boolean value)
    {
        getPrefs().edit().putBoolean(PREF_IS_LOGIN, value).commit();
    }
}
