package com.hitstreamr.hitstreamrbeta;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CreditsRCVAdapter extends RecyclerView.Adapter<CreditsRCVAdapter.CreditViewHolder> {

    private static final String TAG = "CreditsRCVAdapter";
    private ArrayList<String> mCreditVal = new ArrayList<>();
    private ArrayList<String> mCreditPrice = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();
//    private Context mContext;

    public CreditsRCVAdapter(ArrayList<String> mCreditValue, ArrayList<String> mCreditPrices, ArrayList<String> mDescriptions) {
        this.mCreditVal = mCreditValue;
        this.mCreditPrice = mCreditPrices;
        this.mDescription = mDescriptions;
//        this.mContext = mContext;
    }


    @NonNull
    @Override
    public CreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_creditvalueitem, parent, false);

        return new CreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CreditViewHolder holder, int position) {
        Log.e(TAG, "onBindVIewHolder: called.");

        holder.CreditVal.setText(mCreditVal.get(position));
        holder.CreditPrice.setText(mCreditPrice.get(position));
        holder.PriceDescript.setText(mDescription.get(position));

//        holder.CreditPrice.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        holder.CreditVal.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        holder.PriceDescript.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });


        //TODO set up onBindViewHolder

    }

    @Override
    public int getItemCount() {
        return mCreditVal.size();
    }

    public void clear() {
        final int size = mCreditVal.size();
        mCreditVal.clear();
        notifyItemRangeRemoved(0, size);
    }

    class CreditViewHolder extends RecyclerView.ViewHolder {

        TextView CreditVal, CreditPrice, PriceDescript;

        public CreditViewHolder(View itemView) {
            super(itemView);

            CreditVal = itemView.findViewById(R.id.creditvalue);
            CreditPrice = itemView.findViewById(R.id.creditprice);
            PriceDescript = itemView.findViewById(R.id.priceDescription);
        }
    }
}
