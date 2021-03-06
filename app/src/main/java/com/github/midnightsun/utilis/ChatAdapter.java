package com.github.midnightsun.utilis;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.midnightsun.R;
import com.github.midnightsun.structure.MoreBeanType;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {
    //定义五种常量  表示五种条目类型
    public static final int MSG_OTHER = 0;
    public static final int MSG_SELF = 1;
    public static final int MSG_ONLINE = 2;
    public static final int MSG_OFFLINE = 3;
    public static final int MSG_TIME = 4;
    private List<MoreBeanType> mData;

    public ChatAdapter(List<MoreBeanType> data) {
        this.mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建不同的 ViewHolder
        View view;
        //根据viewtype来创建条目
        switch (viewType) {
            case MSG_OTHER:
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.msg_left,parent,false);
                return new MsgLeftHolder(view);

            case MSG_SELF:
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.msg_right,parent,false);
                return new MsgRightHolder(view);

            case MSG_ONLINE:
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.msg_online,parent,false);
                return new MsgOnlineHolder(view);

            case MSG_OFFLINE:
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.msg_online,parent,false);
                return new MsgOfflineHolder(view);

            default:
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.msg_date,parent,false);
                return new MsgDateHolder(view);
        }

    }

    //根据不同的viewholder对view进行操作
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MoreBeanType moreBeanType = mData.get(position);
        if (holder instanceof MsgLeftHolder) {
            ((MsgLeftHolder) holder).imageView.
                    setImageResource(moreBeanType.getResourcesID());
            ((MsgLeftHolder) holder).textView.setText(moreBeanType.getResourcesContent());
        }
        else if (holder instanceof  MsgRightHolder) {
            ((MsgRightHolder) holder).imageView.
                    setImageResource(moreBeanType.getResourcesID());
            ((MsgRightHolder) holder).textView.setText(moreBeanType.getResourcesContent());
        }
        else if (holder instanceof MsgDateHolder) {
            ((MsgDateHolder) holder).dateView.setText(moreBeanType.getResourcesContent());
        }
        else if (holder instanceof MsgOnlineHolder) {
            ((MsgOnlineHolder) holder).textView.setText(moreBeanType.getResourcesContent());
        }
        else {
            ((MsgOfflineHolder) holder).textView.setText(moreBeanType.getResourcesContent());
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    //根据条件返回条目的类型
    @Override
    public int getItemViewType(int position) {
        if (mData.size() > 0) {
            return mData.get(position).getViewType();
        }
        return super.getItemViewType(position);
    }

    /**
     * 创建五种ViewHolder
     */
    public class MsgRightHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public MsgRightHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.msg_right);
            imageView = itemView.findViewById(R.id.chat_img_me);
        }
    }

    public class MsgLeftHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public MsgLeftHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.msg_left);
            imageView = itemView.findViewById(R.id.chat_img);
        }
    }

    public class MsgDateHolder extends RecyclerView.ViewHolder {
        public TextView dateView;
        public MsgDateHolder(View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.msg_date);
        }
    }

    public class MsgOnlineHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public MsgOnlineHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.online_alert);
        }
    }

    public class MsgOfflineHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public MsgOfflineHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.online_alert);
        }
    }
}
