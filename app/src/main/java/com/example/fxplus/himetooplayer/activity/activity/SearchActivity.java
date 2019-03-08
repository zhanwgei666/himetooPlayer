package com.example.fxplus.himetooplayer.activity.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.adapter.SearchAdapter;
import com.example.fxplus.himetooplayer.activity.domain.SearchBean;
import com.example.fxplus.himetooplayer.activity.util.Constants;
import com.google.gson.Gson;
import com.example.fxplus.himetooplayer.activity.util.JsonParser;
import com.example.fxplus.himetooplayer.activity.util.LogUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

//搜索页面
public class SearchActivity extends Activity {
    private EditText etInput;
    private ImageView ivVoice;
    private TextView tvSearch;
    private ListView listview;
    private ProgressBar progressBar;
    private TextView tvNodata;

    //用于HashMap存储听写的结果
    private HashMap<String,String> mIatResults = new LinkedHashMap<String,String>();
    private String url;

    private List<SearchBean.ItemData> items;

    //适配器
    private SearchAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
    }


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-03-05 16:28:21 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        etInput = (EditText)findViewById( R.id.et_input );
        ivVoice = (ImageView)findViewById( R.id.iv_voice );
        tvSearch = (TextView)findViewById( R.id.tv_search );
        listview = (ListView)findViewById( R.id.listview );
        progressBar = (ProgressBar)findViewById( R.id.progressBar );
        tvNodata = (TextView)findViewById( R.id.tv_nodata );

        //设置点击事件
        ivVoice.setOnClickListener(new MyOnClickListener());
        tvSearch.setOnClickListener(new MyOnClickListener());
    }
    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_voice://语音输入
//                    Toast.makeText(SearchActivity.this,"语音输入",Toast.LENGTH_SHORT).show();
                    showDialog();
                    break;
                case R.id.tv_search://搜索
//                    Toast.makeText(SearchActivity.this,"搜索",Toast.LENGTH_SHORT).show();
                    searchText();
                    break;
            }
        }
    }

    private void searchText() {
        String text = etInput.getText().toString().trim();
        if (!TextUtils.isEmpty(text)){
            if (items != null && items.size()>0){
                items.clear();

            }
            try {
                text= URLEncoder.encode(text,"UTF-8");

                 url = Constants.SEARCH_URL + text;

                 getDataFromNet();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDataFromNet() {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void processData(String result) {
        SearchBean searchBean = parsedJson(result);
        items =searchBean.getItems();
        showData();

    }

    private void showData() {
        //设置适配器
        if (items != null && items.size()>0){
            adapter = new SearchAdapter(this,items);
            listview.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.GONE);
    }

    /**
     * 解析json数据
     * @param result
     * @return
     */
    private SearchBean parsedJson(String result) {
        return new Gson().fromJson(result, SearchBean.class);
    }

    private void showDialog() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param recognizerResult
         * @param b                是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
//            Log.e("MainActivitynActivity", "result ==" + result);
            String text = JsonParser.parseIatResult(result);
            //解析好的
//            Log.e("MainActivity", "text ==" + text);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();//拼成一句
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            etInput.setText(resultBuffer.toString());
            etInput.setSelection(etInput.length());

        }

        /**
         * 出错了
         *
         * @param speechError
         */
        @Override
        public void onError(SpeechError speechError) {
//            Log.e("MainActivity", "onError ==" + speechError.getMessage());

        }
    }


    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
