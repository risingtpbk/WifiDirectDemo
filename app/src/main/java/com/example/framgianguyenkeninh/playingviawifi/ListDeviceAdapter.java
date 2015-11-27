package com.example.framgianguyenkeninh.playingviawifi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by FRAMGIA\nguyen.ke.ninh on 25/11/2015.
 */
public class ListDeviceAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<WifiP2pDevice> listData;
    private OnItemClickListener onItemClickListener;

    public ListDeviceAdapter(Context context, ArrayList<WifiP2pDevice> listData){
        this.context = context;
        this.listData = listData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        WifiP2pDevice device = listData.get(position);
        viewHolder.name.setText(device.deviceName + "; " + device.deviceAddress);
    }

    @Override
    public int getItemCount() {
        if(listData != null && !listData.isEmpty()){
            return listData.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public Button btnConnect;
        public Button btnDisconnect;
        public Button btnSend;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            btnConnect = (Button) itemView.findViewById(R.id.btn_connect);
            btnDisconnect = (Button) itemView.findViewById(R.id.btn_disconnect);
            btnSend = (Button) itemView.findViewById(R.id.send);

            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.connect(getLayoutPosition());
                }
            });
            btnDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.disconnect(getLayoutPosition());
                }
            });
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.send();
                }
            });
        }
    }
}
