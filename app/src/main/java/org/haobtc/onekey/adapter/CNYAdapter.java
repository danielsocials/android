package org.haobtc.onekey.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.CNYBean;

import java.util.ArrayList;

public class CNYAdapter extends RecyclerView.Adapter<CNYAdapter.myViewHolder> {
    private Context context;
    private ArrayList<CNYBean> listCNY;

    public CNYAdapter(Context context, ArrayList<CNYBean> listCNY, int cnypos) {
        this.context = context;
        this.listCNY = listCNY;
        this.listCNY.get(cnypos).setStatus(true);
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tetCny;
        ImageView imgCheck;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tetCny = itemView.findViewById(R.id.tet_Cny);
            imgCheck = itemView.findViewById(R.id.img_check);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.cny_item, null);
        return new myViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.tetCny.setText(listCNY.get(position).getName());

        holder.tetCny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < listCNY.size(); i++) {
                    listCNY.get(i).setStatus(false);
                }
                listCNY.get(position).setStatus(true);
                Log.i("onClickJXM", "onClick: " + listCNY.get(position).isStatus());
                onLisennorClick.itemClick(position);
                notifyDataSetChanged();

            }
        });
        if (listCNY.get(position).isStatus()) {
            holder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            holder.imgCheck.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (listCNY != null) {
            return listCNY.size();
        } else {
            return 0;
        }

    }

    public interface onLisennorClick {
        void itemClick(int pos);
    }

    private onLisennorClick onLisennorClick;

    public void setOnLisennorClick(onLisennorClick onLisennorClick) {
        this.onLisennorClick = onLisennorClick;
    }

}
