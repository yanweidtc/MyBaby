package com.hdcy.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hdcy.app.R;
import com.hdcy.app.model.CommentsContent;
import com.hdcy.app.model.Replys;
import com.hdcy.base.BaseInfo;
import com.hdcy.base.utils.BaseUtils;
import com.hdcy.base.utils.RelativeTimeUtils;
import com.hdcy.base.utils.net.NetHelper;
import com.hdcy.base.utils.net.NetRequestCallBack;
import com.hdcy.base.utils.net.NetRequestInfo;
import com.hdcy.base.utils.net.NetResponseInfo;
import com.squareup.picasso.Picasso;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by WeiYanGeorge on 2016-10-11.
 */

public class ArticleCommentListAdapter extends BaseAdapter {

    private List<CommentsContent> data = new ArrayList<>();
    private List<Boolean> status = new ArrayList<>();
    List<Replys> replysList = new ArrayList<>();

    ReplysAdapter replysAdapter;

    private LayoutInflater mInflater;
    private Context context;

    private int actualcount;

    private String replyid;
    private String targetid;
    private String target;

    public ArticleCommentListAdapter(Context context, List<CommentsContent> data, List<Boolean> status){
        super();
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
        this.status = status;
    }

    @Override
    public int getCount() {
        if(data.size() >5 ){
            return 5;
        }else {
            return data.size();
        }
    }

    @Override
    public CommentsContent getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_comments,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
            AutoUtils.autoSize(convertView);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setTag(position);
        setView(position, holder);
        return convertView;
    }

    private void setView(final int position, final ViewHolder holder){
        final CommentsContent item = getItem(position);
        replysList = item.getReplys();
        if(replysList.isEmpty()){
            holder.lv_replys.setVisibility(View.GONE);
        }else {
            holder.lv_replys.setVisibility(View.VISIBLE);
        }
        replysAdapter = new ReplysAdapter(context,replysList);
        holder.lv_replys.setTag(replysList);// set tags
        holder.lv_replys.setAdapter(replysAdapter);
        holder.tv_more.setTag(replysList);
        if(replysList.size()>2){
            holder.tv_more.setVisibility(View.VISIBLE);
            holder.tv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replysList=(ArrayList<Replys>)v.getTag();
                    replysAdapter = new ReplysAdapter(context, replysList);
                    holder.lv_replys.setAdapter(replysAdapter);
                    replysAdapter.addItemNum(replysList.size());
                    replysAdapter.notifyDataSetChanged();
                    holder.tv_more.setVisibility(View.GONE);
                }
            });
        }else {
            holder.tv_more.setVisibility(View.GONE);
        }
        if(!BaseUtils.isEmptyString(item.getCreaterName())) {
            holder.tv_name.setText(item.getCreaterName() + "");
        }
        if(!BaseUtils.isEmptyString(item.getCreaterHeadimgurl())) {
            Picasso.with(context).load((item.getCreaterHeadimgurl()))
                    .placeholder(BaseInfo.PICASSO_PLACEHOLDER)
                    .resize(50, 50)
                    .into(holder.iv_avatar);
        }
        if(!BaseUtils.isEmptyString(item.getCreatedTime().toString())) {
            Date time = item.getCreatedTime();
            String nowdate = RelativeTimeUtils.format(time);

            holder.tv_time.setText(nowdate);
        }
        if(!BaseUtils.isEmptyString(item.getContent())){
        holder.tv_comment_content.setText(item.getContent());
        }
        holder.tv_praise_count.setText(item.getPraiseCount()+"");
        if(item.isLike()){
            holder.iv_praise.setImageResource(R.drawable.content_press_praise);
           // holder.iv_praise.setVisibility(View.VISIBLE);
        }else {
            holder.iv_praise.setImageResource(R.drawable.content_con_zambia_default);
            //holder.iv_praise.setVisibility(View.GONE);
        }
        actualcount = item.getPraiseCount();
        final String targetId = item.getId() + "";
        holder.iv_praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(context,   actualcount+"点击位置"+position, Toast.LENGTH_SHORT).show();
                int tag = (int) holder.getTag();
                if(tag == position) {
                    if (item.isLike()==true) {
                        NetHelper.getInstance().UnDoPraise(targetId, new NetRequestCallBack() {
                            @Override
                            public void onSuccess(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {
                                holder.iv_praise.setImageResource(R.drawable.content_con_zambia_default);
                                item.setLike(false);
                                int number = actualcount;
                                holder.tv_praise_count.setText(number + "");
                            }

                            @Override
                            public void onError(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                            }

                            @Override
                            public void onFailure(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                            }
                        });
                    } else {
                        NetHelper.getInstance().DoPraise(targetId, new NetRequestCallBack() {
                            @Override
                            public void onSuccess(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {
                                holder.iv_praise.setImageResource(R.drawable.content_press_praise);
                                item.setLike(true);
                                int number = actualcount + 1;
                                holder.tv_praise_count.setText(number + "");

                            }

                            @Override
                            public void onError(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                            }

                            @Override
                            public void onFailure(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                            }
                        });
                    }
                }
            }
        });



    }

    public class ViewHolder {
        private ImageView iv_avatar;
        private ImageView iv_praise;
        private TextView tv_name, tv_praise_count, tv_time, tv_comment_content;
        private ListView lv_replys;
        private LinearLayout ly_sub_replys;
        private LinearLayout tv_more;

        private Object tag;

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public ViewHolder(View itemView) {

            ButterKnife.bind(this, itemView);

            iv_avatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
            iv_praise = (ImageView) itemView.findViewById(R.id.iv_praise);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_praise_count = (TextView) itemView.findViewById(R.id.tv_praise_count);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            ly_sub_replys = (LinearLayout) itemView.findViewById(R.id.ly_sub_reply);
            lv_replys = (ListView) itemView.findViewById(R.id.lv_replys);
            tv_comment_content = (TextView) itemView.findViewById(R.id.tv_comment_content);
            tv_more = (LinearLayout) itemView.findViewById(R.id.tv_more);

        }
    }


}
