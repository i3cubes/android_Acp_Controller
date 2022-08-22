package com.i3cubes.acp_controller;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    ArrayList<device> mValues;
    Context mContext;
    protected ItemListener mListener;

    public RecycleViewAdapter(Context context, ArrayList<device> values,ItemListener itemListener){
        mValues = values;
        mContext = context;
        mListener=itemListener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textView;
        public ImageView imageView;
        public RelativeLayout relativeLayout;
        public ImageView sig_level;
        device d;

        public ViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            ///textView=(TextView) v.findViewById(R.id.textView);
            ///imageView=(ImageView) v.findViewById(R.id.imageView);
            ///relativeLayout=(RelativeLayout) v.findViewById(R.id.relativeLayout);
            ///sig_level=(ImageView) v.findViewById(R.id.img_sig_level);

        }
        public void setData(device dev){
            this.d=dev;
            String name_n="";
            if(d.isBLE){
                name_n=d.name;
            }
            else{
                name_n=d.name.substring(0,11);
            }
            textView.setText(name_n);
            sig_level.setImageResource(this.getImageResource());
            //imageView.setImageResource(dev.drawable);
            //relativeLayout.setBackgroundColor(Color.parseColor(dev.color));
        }
        private Integer getImageResource(){
            Integer id;
            switch (d.getSignalBars()){
                case 1:
                    ///id=R.drawable.p_signal_1;
                    break;
                case 2:
                    ///id=R.drawable.p_signal_2;
                    break;
                case 3:
                    ///id=R.drawable.p_signal_3;
                    break;
                case 4:
                    ///id=R.drawable.p_signal_4;
                    break;
                case 5:
                    ///id=R.drawable.p_signal_5;
                    break;
                default:
                    ///id=R.drawable.p_signal_1;
            }
            ///return id;
            return 0;
        }
        @Override
        public void onClick(View view){
            System.out.println("clicked");
            if(mListener !=null){
                textView.setTextColor(view.getResources().getColor(R.color.meroon));
                mListener.onItemClick(d,view);
            }
            else{
                System.out.println("mListener is null");
            }
        }
    }

    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       /// View view = LayoutInflater.from(mContext).inflate(R.layout.bt_tile_item,parent,false);
        ///return new ViewHolder(view);
        return  null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface ItemListener{
        void onItemClick(device d,View v);
    }
}
