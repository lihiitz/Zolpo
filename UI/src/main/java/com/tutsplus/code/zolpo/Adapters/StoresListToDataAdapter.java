package com.tutsplus.code.zolpo.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tutsplus.code.zolpo.Models.ImageLoader;
import com.tutsplus.code.zolpo.R;
import com.tutsplus.code.zolpo.Models.StoreListItem;

import java.util.List;

/**
 To connect the data to the listItem
 **/
public class StoresListToDataAdapter extends RecyclerView.Adapter<StoresListToDataAdapter.ViewHolder> {

    private static final String TAG = "StoresListToDataAdapter";
    private List<StoreListItem> m_ListItems;
    private Context m_Context;

    public StoresListToDataAdapter(List<StoreListItem> m_ListItems, Context m_Context) {
        this.m_ListItems = m_ListItems;
        this.m_Context = m_Context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    /**
      Binds the data to the recycler
     **/
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder: Binds the data to the recycler...");
        final StoreListItem listItem = m_ListItems.get(position); // give the specific listItem I want to bind to data
        String formattedDistance;
        //Store Name
        holder.storeNameTextView.setText(listItem.getFullStoreName());
        //Store Address
        holder.storeAddressTextView.setText(listItem.getFullAddress());
        //Distance
        formattedDistance = String.format("%.2f", listItem.getDistance());
        holder.distanceTextView.setText(String.format("%1s ק\"מ ממיקומך", formattedDistance));
        //Price
        holder.productPriceTextView.setText("₪" + Double.toString(listItem.getProductPrice()));
        //Promotion
        bindPromotionToView(holder, listItem);
        //Store Image
        ImageLoader.StaticImageLoader.LoadImageFromUrl(m_Context, listItem.getChainImageURL(), holder.chainImageView, R.drawable.ic_question_mark_cart);
    }

    private void bindPromotionToView(@NonNull final ViewHolder holder, StoreListItem iListItem) {
        if (iListItem.getPromotion() != null) {
            holder.promotionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_thumb_up, 0);
            holder.promotionTextView.setText(Html.fromHtml("<font color=#008000><b> מבצע! </b></font>"));
            holder.promotionTextView.append("\n"+iListItem.getPromotion());
            holder.promotionTextView.setTextColor(Color.BLACK);
        } else
        {
            holder.promotionTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_thumb_down, 0);
            holder.promotionTextView.setText(" אין מבצעים");
            holder.promotionTextView.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return m_ListItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView storeNameTextView, storeAddressTextView, productPriceTextView, distanceTextView,
                promotionTextView, productNameTextView;
        public ImageView chainImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
            storeAddressTextView = itemView.findViewById(R.id.storeAddressTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            promotionTextView = itemView.findViewById(R.id.promotionTextView);
            chainImageView = itemView.findViewById(R.id.chainImageView);
        }
    }
}
