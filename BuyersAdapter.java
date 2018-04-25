package com.springtech.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.springtech.R;
import com.springtech.app.App;
import com.springtech.database.MessagesDataSource;
import com.springtech.fragment.buyerlist.BuyerView;
import com.springtech.listener.OnItemClickListener;
import com.springtech.listener.OnLoadMoreListener;
import com.springtech.permissionutils.Func;
import com.springtech.permissionutils.PermissionUtil;
import com.springtech.requestmodel.GetBuyerList;
import com.springtech.responsemodel.GetBuyerListResponse;
import com.springtech.responsemodel.GetFarmerListModel;
import com.springtech.utils.IconView;
import com.springtech.utils.SimpleSpanBuilder;
import com.springtech.utils.Utils;

import java.util.ArrayList;

import static android.Manifest.permission.READ_SMS;
import static com.springtech.permissionutils.PermissionUtil.REQUEST_CODE_PERMISSION;

/**
 * Created by nilesh.patil on 05-02-2018.
 */

public class BuyersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private boolean isLoading;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private OnLoadMoreListener mOnLoadMoreListener;

    private Context mContext;
    private ArrayList<GetBuyerListResponse.BuyerList> list;
    private ArrayList<GetBuyerListResponse.BuyerList> mFilteredlist;
    private OnItemClickListener onItemClickListener;
    private BuyersListFilter buyersListFilter = new BuyersListFilter();
    private BuyerView mView;

    public BuyersAdapter(Context context, BuyerView buyerView, ArrayList<GetBuyerListResponse.BuyerList> list,
                         OnItemClickListener onItemClickListener, RecyclerView recyclerview) {
        this.mContext = context;
        this.list = list;
        this.mFilteredlist = list;
        this.onItemClickListener = onItemClickListener;
        this.mView = buyerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_buyers, parent, false);
        Utils.setFont((ViewGroup) view.findViewById(R.id.parent), App.getRegularFont());
        vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {
        ViewHolder holder = (ViewHolder) viewholder;
        final GetBuyerListResponse.BuyerList model = mFilteredlist.get(position);
        holder.mTxtName.setText(getSimpleSpanBuilder(mContext.getString(R.string.FL_name) + " : ", model.getName()).build());
        holder.mTxtVillage.setText(getSimpleSpanBuilder(mContext.getString(R.string.Village) + " : ", model.getVillage()).build());
        holder.mTxtTalDist.setText(getSimpleSpanBuilder(mContext.getString(R.string.FL_tal_dist) + " : ", model.getTaluka()).build());
        holder.mTxtMobile.setText(getSimpleSpanBuilder(mContext.getString(R.string.FL_mobile) + " : ", model.getMobile()).build());
        holder.mTxtPrice.setText(getSimpleSpanBuilder(mContext.getString(R.string.CL_price) + " : ", model.getPrice()).build());

    }

    @NonNull
    private SimpleSpanBuilder getSimpleSpanBuilder(String label, String value)
    {
        SimpleSpanBuilder spannableStringBuilder = new SimpleSpanBuilder();
        spannableStringBuilder.append(label, new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.black)), new RelativeSizeSpan(1));
        spannableStringBuilder.append(value, new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.text_grayfaint)), new RelativeSizeSpan(1));
        return spannableStringBuilder;
    }

    @Override
    public int getItemCount()
    {
        return mFilteredlist == null ? 0 : mFilteredlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        protected TextView mTxtName, mTxtVillage, mTxtTalDist, mTxtMobile, mTxtPrice;
        protected RelativeLayout mLayCallToAskPrice;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTxtName = (TextView) itemView.findViewById(R.id.txtName);
            mTxtMobile = (TextView) itemView.findViewById(R.id.txtMobile);
            mTxtVillage = (TextView) itemView.findViewById(R.id.txtVillage);
            mTxtTalDist = (TextView) itemView.findViewById(R.id.txtTalDist);
            mTxtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
            mLayCallToAskPrice = (RelativeLayout) itemView.findViewById(R.id.layCallToAsk);
            mLayCallToAskPrice.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view)
        {
            switch(view.getId())
            {
                case R.id.layCallToAsk:
                    Utils.makeCall(mContext,mFilteredlist.get(getAdapterPosition()).getMobile());
                    break;
            }
        }
    }



    @Override
    public Filter getFilter()
    {
        if (buyersListFilter == null)
        {
            buyersListFilter = new BuyersAdapter.BuyersListFilter();
        }
        return buyersListFilter;
    }

    private class BuyersListFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String charString = constraint.toString();
            if (charString.isEmpty())
            {
                mFilteredlist = list;
            } else
            {
                ArrayList<GetBuyerListResponse.BuyerList> filteredList = new ArrayList<>();
                for (GetBuyerListResponse.BuyerList row : list)
                {
                    if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getMobile().contains(charString))
                    {
                        filteredList.add(row);
                    }
                }

                mFilteredlist = filteredList;
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = mFilteredlist;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            mFilteredlist = (ArrayList<GetBuyerListResponse.BuyerList>) results.values;

            if (mFilteredlist != null)
            {
                if (mFilteredlist.size() == 0)
                {
                    mView.showEmptyView(true);
                } else
                {
                    mView.showEmptyView(false);
                }
            }

            notifyDataSetChanged();
        }
    }
}
