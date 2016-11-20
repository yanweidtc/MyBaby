package com.hdcy.app.fragment.third;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hdcy.app.R;
import com.hdcy.app.adapter.CommonsAdapter;
import com.hdcy.app.adapter.ThirdPageFragmentAdapter;
import com.hdcy.app.basefragment.BaseLazyMainFragment;
import com.hdcy.app.event.StartBrotherEvent;
import com.hdcy.app.fragment.first.FirstFragment;
import com.hdcy.app.model.ActivityContent;
import com.hdcy.app.model.RootListInfo;
import com.hdcy.app.view.ScaleInTransformer;
import com.hdcy.base.BaseInfo;
import com.hdcy.base.utils.BaseUtils;
import com.hdcy.base.utils.SizeUtils;
import com.hdcy.base.utils.net.NetHelper;
import com.hdcy.base.utils.net.NetRequestCallBack;
import com.hdcy.base.utils.net.NetRequestInfo;
import com.hdcy.base.utils.net.NetResponseInfo;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * Created by WeiYanGeorge on 2016-10-07.
 */

public class ThirdFragment extends BaseLazyMainFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private static final String TAG = "ThirdFragment";
    private Toolbar mToolbar;
    private TextView title;
    private ListView mListView;
    private BGARefreshLayout mRefreshLayout;
    private BGABanner mBanner;

    private ThirdPageFragmentAdapter mAdapter;

    private List<ActivityContent> activityContentList = new ArrayList<>();
    private List<ActivityContent> activityHotList = new ArrayList<>();


    private List<String> imgurls = new ArrayList<>();
    private List<String> tips = new ArrayList<>();


    private ViewPager mViewPager;
    private PagerAdapter mAdapter4Banner;
    private CommonsAdapter mAdapters;

    private RootListInfo rootListInfo = new RootListInfo();


    private int pagecount = 0;
    private boolean isLast;

    private String activityType;

    private int width;

    private int imgheight;




    public static ThirdFragment newInstance() {

        Bundle args = new Bundle();

        ThirdFragment fragment = new ThirdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initLazyView(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void initLazyData() {
        initData();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        activityContentList.clear();
        pagecount = 0;
        initData();
        mRefreshLayout.endRefreshing();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        pagecount++;
        if(isLast){
            mRefreshLayout.endLoadingMore();
            Toast.makeText(getActivity(), "没有更多的数据了" , Toast.LENGTH_SHORT).show();
            return false;
        }else {
            GetActivityList();
            return true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third_page,container,false);
        width = SizeUtils.getScreenWidth();
        imgheight = SizeUtils.dpToPx(200);
        initView(view);
        GetTopActivityListBanner();
        setListener();
        return view;
    }

    private void initView(View view){
        mListView = (ListView) view.findViewById(R.id.lv_activity);
        mRefreshLayout = (BGARefreshLayout) view.findViewById(R.id.refresh_layout);
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(getContext(),true));
        mAdapter = new ThirdPageFragmentAdapter(getContext(), activityContentList);
        View headview = View.inflate(getContext(), R.layout.item_headerview_third,null);
        mBanner = (BGABanner) headview.findViewById(R.id.banner);
        mListView.addHeaderView(headview);
        mBanner.setAdapter(new BGABanner.Adapter() {
            @Override
            public void fillBannerItem(BGABanner banner, View view, Object model, int position) {
                Glide.with(banner.getContext()).load(model).placeholder(R.mipmap.icon_chat_camera).error(R.mipmap.icon_chat_camera).dontAnimate().thumbnail(0.1f).into((ImageView) view);

            }
        });
        mListView.setAdapter(mAdapter);

    }

    private void initData(){
        GetNewActivityList();
    }

    private void setListener(){
        mBanner.setOnItemClickListener(new BGABanner.OnItemClickListener() {
            @Override
            public void onBannerItemClick(BGABanner banner, View view, Object model, int position) {
                EventBus.getDefault().post(new StartBrotherEvent(OfflineActivityFragment.newInstance(activityHotList.get(position).getId()+"")));
            }
        });
        mAdapter.setOnItemClickListener(new ThirdPageFragmentAdapter.OnItemClickListener() {
            @Override
            public void onItem(int position) {
                String ActivityId = activityContentList.get(position).getId() + "";
                EventBus.getDefault().post(new StartBrotherEvent(OfflineActivityFragment.newInstance(ActivityId)));
            }
        });
    }

    private void setData1(){
        mBanner.setData(imgurls,tips);
    }

    private void setData(){
        mAdapter.notifyDataSetChanged();
        mRefreshLayout.endLoadingMore();
    }

    private void GetNewActivityList(){
        NetHelper.getInstance().GetActivityNew(null, pagecount, new NetRequestCallBack() {
            @Override
            public void onSuccess(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {
                List<ActivityContent> temp = responseInfo.getActivityContentList();
                activityContentList.addAll(temp);
                GetActivityList();
            }

            @Override
            public void onError(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

            }

            @Override
            public void onFailure(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

            }
        });
    }

    private void GetActivityList(){
        NetHelper.getInstance().GetPaticipationList(null, pagecount, new NetRequestCallBack() {
            @Override
            public void onSuccess(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {
                List<ActivityContent> activityContentstemp = responseInfo.getActivityContentList();
                activityContentList.addAll(activityContentstemp);
                rootListInfo = responseInfo.getRootListInfo();
                isLast = rootListInfo.isLast();
                Log.e("ActivityContentList",activityContentList.size()+"");
                setData();
            }

            @Override
            public void onError(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

            }

            @Override
            public void onFailure(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

            }
        });
    }

    public void GetTopActivityListBanner(){
        if(activityHotList.isEmpty()) {
            NetHelper.getInstance().GetActivityTopBanner(new NetRequestCallBack() {
                @Override
                public void onSuccess(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                    List<ActivityContent> hottemp = responseInfo.getActivityContentList();
                    activityHotList.addAll(hottemp);
                    for (int i = 0; i < activityHotList.size(); i++) {
                        imgurls.add(i, activityHotList.get(i).getImage());
                        tips.add(i, activityHotList.get(i).getName());
                    }
                    setData1();
                }


                @Override
                public void onError(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                }

                @Override
                public void onFailure(NetRequestInfo requestInfo, NetResponseInfo responseInfo) {

                }
            });
        }else {
            imgurls.clear();
            tips.clear();
            for (int i = 0; i < activityHotList.size(); i++) {
                imgurls.add(i, activityHotList.get(i).getImage());
                tips.add(i, activityHotList.get(i).getName());
            }
            setData1();
        }
    }

    private void initHeaderBanner(){
        mViewPager.setPageMargin(40);
        mViewPager.setOffscreenPageLimit(3);
        mAdapter4Banner = new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ActivityContent item = activityHotList.get(position);
                View view = View.inflate(getActivity(),R.layout.item_activity_list,null);
                container.addView(view);
                TextView tv_actvity_title = (TextView) view.findViewById(R.id.tv_activity_title);
                TextView tv_activity_subtitle = (TextView) view.findViewById(R.id.tv_activity_subtitle);
                tv_actvity_title.setText(item.getName()+"");
                SimpleDateFormat foramt = new SimpleDateFormat("yyyy-MM-dd");
                Date startTime = item.getStartTime();
                String dateformat1 = "";
                if(!BaseUtils.isEmptyString(startTime.toString())) {
                    dateformat1 = foramt.format(startTime);
                }
                String subtitle = item.getAddress() +"/"+ dateformat1;
                Log.e("activitytime",item.getStartTime().toGMTString());
                tv_activity_subtitle.setText(subtitle);
                ImageView iv_activity_background =(ImageView) view.findViewById(R.id.iv_activity_background);
                iv_activity_background.setScaleType(ImageView.ScaleType.FIT_XY);
                if(!BaseUtils.isEmptyString(item.getImage())) {
                    String cover = item.getImage();
                    Picasso.with(getContext()).load(cover)
                            .placeholder(BaseInfo.PICASSO_PLACEHOLDER)
                            .resize(width,imgheight)
                            //.centerCrop()
                            .config(Bitmap.Config.RGB_565)
                            .into(iv_activity_background);
                }
                ImageView iv_activity_status = (ImageView) view.findViewById(R.id.iv_activity_status);
                iv_activity_status.setVisibility(View.GONE);
/*                if(item.getFinish()==true){
                    Log.e("activitystatus",item.getFinish()+"1");
                    iv_activity_status.setTag(position);
                    iv_activity_status.setVisibility(View.VISIBLE);
                }else {
                    iv_activity_status.setVisibility(View.GONE);
                }*/
                view.setTag(item);

                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return activityHotList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };

        mViewPager.setAdapter(mAdapter4Banner);
        mViewPager.setPageTransformer(true, new ScaleInTransformer());
    }
}
