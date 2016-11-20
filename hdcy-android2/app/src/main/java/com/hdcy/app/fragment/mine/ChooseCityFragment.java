package com.hdcy.app.fragment.mine;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hdcy.app.R;
import com.hdcy.app.adapter.CityAdapter;
import com.hdcy.app.basefragment.BaseBackFragment;
import com.hdcy.app.model.CityEntity;
import com.hdcy.app.permission.PermissionsActivity;
import com.hdcy.app.permission.PermissionsChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.indexablerv.IndexableAdapter;
import me.yokeyword.indexablerv.IndexableLayout;
import me.yokeyword.indexablerv.SimpleHeaderAdapter;

/**
 * Created by WeiYanGeorge on 2016-11-02.
 */

public class ChooseCityFragment extends BaseBackFragment {

    private List<CityEntity> mDatas = new ArrayList<>();
    private CityAdapter mAdapter;
    private FrameLayout mProgressBar;

    String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private PermissionsChecker mPermissionsChecker;
    private static final int REQUEST_CODE = 0;

    private LocationClient locationClient;

    public static ChooseCityFragment newInstance(){
        Bundle args = new Bundle();
        ChooseCityFragment fragment = new ChooseCityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_city, container, false);
        mPermissionsChecker = new PermissionsChecker(getActivity());
        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPermissionsChecker.lacksPermissions(perms)){
            startPermissionsActivity();
        }
    }
    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(getActivity(), REQUEST_CODE, perms);
    }

    private void initView(View view){
        //百度地图初始化
        locationClient = new LocationClient(getActivity());

        IndexableLayout indexableLayout = (IndexableLayout) view.findViewById(R.id.indexableLayout);
        mProgressBar = (FrameLayout) view.findViewById(R.id.progress);
        mAdapter = new CityAdapter(getContext());
        indexableLayout.setAdapter(mAdapter);
        mDatas = initDatas();
        indexableLayout.setFastCompare(true);
        mAdapter.setDatas(mDatas, new IndexableAdapter.IndexCallback<CityEntity>() {
            @Override
            public void onFinished(List<CityEntity> datas) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mAdapter.setOnItemContentClickListener(new IndexableAdapter.OnItemContentClickListener<CityEntity>() {
            @Override
            public void onItemClick(View v, int originalPosition, int currentPosition, CityEntity entity) {
                //Toast.makeText(getContext(),"选中: " + entity.getName() +" 当前位置:"+ currentPosition,Toast.LENGTH_SHORT ).show();
                Bundle bundle = new Bundle();
                bundle.putString(MineInfoFragment.KEY_RESULT_CHOOSE_CITY,entity.getName());
                setFramgentResult(RESULT_OK,bundle);
                _mActivity.onBackPressed();
            }
        });
        final List<CityEntity> gpsCity = iniyGPSCityDatas();
        final SimpleHeaderAdapter gpsHeaderAdapter = new SimpleHeaderAdapter<>(mAdapter,"定","当前城市",gpsCity);
        indexableLayout.addHeaderAdapter(gpsHeaderAdapter);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);// 配置获取详细地址
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if(bdLocation !=null){
                    Log.e("haha",bdLocation.getCity());
                    gpsCity.get(0).setName(bdLocation.getCity());
                    gpsHeaderAdapter.notifyDataSetChanged();
                }else {
                    gpsCity.get(0).setName("定位失败,请手动选择");
                    gpsHeaderAdapter.notifyDataSetChanged();
                }
            }
        });
        locationClient.start();
/*        indexableLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                gpsCity.get(0).setName("北京");
                gpsHeaderAdapter.notifyDataSetChanged();
            }
        },3000);*/

    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);// 配置获取详细地址
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if(bdLocation !=null){
                    String city = bdLocation.getCity();

                }
            }
        });

    }


    private List<CityEntity> iniyGPSCityDatas() {
        List<CityEntity> list = new ArrayList<>();
        list.add(new CityEntity("定位中..."));
        return list;
    }

    private List<CityEntity> initDatas(){
        List<CityEntity> list = new ArrayList<>();
        List<String> cityStrings = Arrays.asList(getResources().getStringArray(R.array.city_array));
        for (String item : cityStrings){
            CityEntity cityEntity = new CityEntity();
            cityEntity.setName(item);
            list.add(cityEntity);
        }
        return list;
    }
}
