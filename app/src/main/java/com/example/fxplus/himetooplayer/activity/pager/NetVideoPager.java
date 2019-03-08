package com.example.fxplus.himetooplayer.activity.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.activity.SystemVideoPlayer;
import com.example.fxplus.himetooplayer.activity.adapter.NetVideoPagerAdapter;
import com.example.fxplus.himetooplayer.activity.base.BasePager;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;
import com.example.fxplus.himetooplayer.activity.util.CacheUtils;
import com.example.fxplus.himetooplayer.activity.util.Constants;
import com.example.fxplus.himetooplayer.activity.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;


//网络视频页面

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.mlistView)
    private ListView mListView;

    @ViewInject(R.id.tv_nonet)
     private TextView mTv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mprogressBar;

    //装数据的集合
    private ArrayList<MediaItem> mediaItems;

    public NetVideoPager(Context context) {
        super(context);
    }

    //定义适配器
    private NetVideoPagerAdapter adpater;

    @Override
    public View initView() {
        LogUtil.e("网络视频页面被初始化");
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        //第一个参数是NetVideoPager.this,第二个参数是布局。
        x.view().inject(this,view);
        //设置ListView的Item的点击事件
        mListView.setOnItemClickListener(new NetVideoPager.MyOnItemClickListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }


    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络视频页面数据被初始化了");
        //判断有误网络，缓存数据
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if (!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }
        getDataFromNet();


    }

    private void getDataFromNet() {
        //联网
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功==" + result);
                //缓存数据
                CacheUtils.putString(context,Constants.NET_URL,result);
                //解析数据
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败==" + ex.getMessage());
              show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");

            }
        });
    }

    //解析数据
    private void processData(String json) {
    mediaItems = parseJson(json);
    //设置配置器
        show();

    }

    //适配器
    public void show(){
        if (mediaItems != null && mediaItems.size() > 0) {
            //有数据
            //设置适配器
            adpater = new NetVideoPagerAdapter(context,mediaItems);
            mListView.setAdapter(adpater);
            //把文本隐藏
            mTv_nonet.setVisibility(View.GONE);
        } else {
            //没有数据
            //文本显示
            mTv_nonet.setVisibility(View.VISIBLE);
        }
        mprogressBar.setVisibility(View.GONE);
    }
    /**
     * 解析数据有两种方式：
     * 1、用系统的接口解析Json数据
     * 2、用第三方解析工具解析（Gson,fastjson）
     */
    private ArrayList<MediaItem> parseJson(String json) {
    ArrayList<MediaItem> mediaItems = new ArrayList<>();
    try {
        JSONObject jsonObject = new JSONObject(json);
       JSONArray jsonArray = jsonObject.optJSONArray("trailers");
       if (jsonArray != null && jsonArray.length() > 0){
           for (int i = 0;i <jsonArray.length();i++){
               JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
               if (jsonObjectItem != null){
                   MediaItem mediaItem = new MediaItem();
                String movieName = jsonObjectItem.optString("movieName");//name
                   mediaItem.setName(movieName);
                String videoTitle = jsonObjectItem.optString("videoTitle");//desc
                   mediaItem.setDesc(videoTitle);
                String imageUrl = jsonObjectItem.optString("coverImg");//ImgUrl
                   mediaItem.setImageUrl(imageUrl);
                String hightUrl = jsonObjectItem.optString("hightUrl");//data
                   mediaItem.setData(hightUrl);

                   //把数据添加到集合
                   mediaItems.add(mediaItem);
               }
           }
       }
    } catch (JSONException e) {
        e.printStackTrace();
    }
        return mediaItems;
    }
}
