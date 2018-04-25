
package com.springtech.service;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import com.springtech.app.App;
import com.springtech.database.DBQueries;
import com.springtech.database.MessagesDataSource;
import com.springtech.jsonrequest.MakeRequest;
import com.springtech.listener.ResponseListener;
import com.springtech.requestmodel.GetAllCropList;
import com.springtech.responsemodel.GetAllCropListResponse;
import com.springtech.utils.SharedPrefrancesValue;
import com.springtech.utils.Utils;

import static com.springtech.app.Constant.COMMAND_GetAllCropList;
import static com.springtech.database.DBQueries.COLUMN_CROP_DETAIL_ID;

public class GetAllCropListService extends IntentService
{
    private String TAG = "GetFarmersService";

    public GetAllCropListService()
    {
        super("GetFarmersService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        GetAllCropList model = new GetAllCropList();
        model.setCommandType(COMMAND_GetAllCropList);
        model.setLocale(Utils.getLocale());
        model.setLastCheckedDtm(SharedPrefrancesValue.getInstance().getAllCropLastCheckedDtm());
        MakeRequest.getInstance().jsonObjectRequest(model, GetAllCropListResponse.class, new ResponseListener()
        {
            @Override
            public void onResponse(Object responseModel)
            {
                GetAllCropListResponse response = (GetAllCropListResponse) responseModel;

                if (!response.isError())
                {
//
                    MessagesDataSource.getmMessageDataSourceInstance(App.getInstance()).saveCrop(response.getResult().getAllCropList());

                    for (String deletedId : response.getResult().getDeletedCropList())
                    {
                        MessagesDataSource.getmMessageDataSourceInstance(App.getInstance()).delete(DBQueries.TABLE_CROP, COLUMN_CROP_DETAIL_ID, deletedId);
                    }

                    SharedPrefrancesValue.getInstance().setAllCropLastCheckedDtm(response.getResult().getLastCheckedDtm());

//                    Intent intent = new Intent();
//                    intent.setAction(Constant.INTENT_ON_GET_FARMER);
//                    sendBroadcast(intent);
                } else
                {
                    Toast.makeText(GetAllCropListService.this, "" + response.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error)
            {
                Toast.makeText(GetAllCropListService.this, "" + error, Toast.LENGTH_LONG).show();
            }
        });
    }


//    registerReceiver(mGetPrescriptionDataBroadcastReceiver, new IntentFilter(Constants.CHECK_PRESCRIPTION_DATA));
//    Intent intent = new Intent(this, GetPrescriptionData.class);
//    startService(intent);

//    @Override
//    protected void onDestroy()
//    {
//        super.onDestroy();
//        unregisterReceiver(mGetPrescriptionDataBroadcastReceiver);
//    }

//    BroadcastReceiver mGetPrescriptionDataBroadcastReceiver = new BroadcastReceiver()
//    {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            //rebind list here
//        }
//    };

}
