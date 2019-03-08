package com.example.fxplus.himetooplayer.activity.activity;

//作用：系统播放器

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;
import com.example.fxplus.himetooplayer.activity.util.Utils;
import com.example.fxplus.himetooplayer.activity.view.VitamioVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;



public class VitamioVideoPlayer extends Activity implements View.OnClickListener {
    private boolean isUseSystem = true;//判断用系统监听卡或者是自定义监听卡
    private static final int PROGRESS = 1;//视频进度的更新
    private static final int HIDE_MEDIACONTROLLER = 2;//隐藏控制面板
    private static final int SHOW_SPEED = 3;//显示网速
    private static final int FULL_SCREEN = 1;    //全屏
    private static final int DEFAULT_SCREEN = 2;//默认屏幕
    private VitamioVideoView videoview;
    private Uri uri;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private RelativeLayout media_controller;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private TextView tv_loading_netspeed;
    private LinearLayout ll_loading;

    private Utils utils;
    //监听电量变化的广播
    private MyReceiver receiver;
    //传入进来的视屏列表
    private ArrayList<MediaItem> mediaItems;
    //要播放列表中的具体位置
    private int position;
    //1、定义手势识别器
    private GestureDetector detector;
    //是否显示控制面板
    private boolean isshowMediaController = false;
    //调用声音
    private AudioManager am;
    //当前声音
    private int currentVoice;
    //最大音量（0到15个等级）
    private int maxVoice;
    //是否静音
    private boolean isMute = false;
    //获取屏幕的宽
    private int screenWidth = 0;
    //获取屏幕的高
    private int screenHeight = 0;
    //是否是网络的URL
    private boolean isNetUri;
    //上一次播放进度
    private int precurrentPosition;
    //是否全屏切换
    private boolean isFullScreen = false;
    //真实视频的宽高
    private int videoWidth ;
    private int videoHeight;


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-02-18 14:22:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_vitamio_video_player);
        videoview =  (VitamioVideoView) findViewById(R.id.videoview);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwichPlayer = (Button) findViewById(R.id.btn_swich_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = (Button) findViewById(R.id.btn_video_siwch_screen);
        media_controller = (RelativeLayout) findViewById(R.id.media_controller);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

        btnVoice.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);

        //最大音量和SeekBar关联
        seekbarVoice.setMax(maxVoice);
        //设置当前进度-当前音量
        seekbarVoice.setProgress(currentVoice);
        //开始更新网络速度
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2019-02-18 14:22:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute = !isMute;
            // Handle clicks for btnVoice
            updataVoice(currentVoice, isMute);
        } else if (v == btnSwichPlayer) {
            // Handle clicks for btnSwichPlayer
            showSwichPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            playNextVideo();
        } else if (v == btnVideoSiwchScreen) {
            // Handle clicks for btnVideoSiwchScreen
            setFullScreenAndDefault();
        }

        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提醒您");
        builder.setMessage("当您播放一个视频，有花屏时，请切换使用系统播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSystemPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void startSystemPlayer() {
        if(videoview != null){
            videoview.stopPlayback();
        }
        Intent intent = new Intent(this,SystemVideoPlayer.class);
        if(mediaItems != null && mediaItems.size() > 0){
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        }else if(uri != null){
            intent.setData(uri);
        }
        startActivity(intent);

        finish();//关闭页面
    }

    //暂停和播放
    private void startAndPause() {
        if (videoview.isPlaying()) {
            //视频播放 --> 视频暂停
            videoview.pause();
            //按钮设置状态播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            //视频播放
            videoview.start();
            //按钮设置状态为暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    //播放上一个视频
    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放上一个
            position--;
            if (position >= 0) {
                //把加载页显示
                ll_loading.setVisibility(View.GONE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                //设置按钮状态
                setButtonState();
            } else if (uri != null) {
                //设置按钮转态-->上一个下一个按钮为灰色
                setButtonState();
            }
        }
    }

    //播放下一个视频
    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position < mediaItems.size()) {
                //把加载页显示
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                //设置按钮状态
                setButtonState();
            } else if (uri != null) {
                //设置按钮转态-->上一个下一个按钮为灰色
                setButtonState();
            }
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                //两个按钮设置为灰色，并不可按
                setEnable(false);
            } else if (mediaItems.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);

                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置为灰色，并不可按
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED://显示网速
                    //得到网速
                    String netSpeed = utils.getNetSpeed(VitamioVideoPlayer.this);
                    //赋值网速
                    tv_loading_netspeed.setText("玩命加载中" + netSpeed);
                    tv_buffer_netspeed.setText("玩命加载中" + netSpeed);
                    //每两秒更新一次
                    removeMessages(SHOW_SPEED);
                    sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
                case HIDE_MEDIACONTROLLER://隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:
                    //1、得到当前的视频播放进程
                    int currentPosition = (int) videoview.getCurrentPosition();
                    //2、seekbarVideo.setProgress()当前进度
                    seekbarVideo.setProgress(currentPosition);
                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //设置系统时间
                    tvSystemTime.setText(getSysteTime());
                    //缓存进度的更新
                    if (isNetUri) {
                        //只有网络资源才有缓存效果
                        int buffer = videoview.getBufferPercentage();
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓存效果
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    //监听卡
                    if (!isUseSystem && videoview.isPlaying()) {
                        if (videoview.isPlaying()) {
                            //得到进度更新，当前进度-上一次进度
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                //小于500毫秒，视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    precurrentPosition = currentPosition;

                    //3、每秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        setListener();
        getData();
        setData();

    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());//设置视频名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoview.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tvName.setText(uri.toString());//设置视频名称
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);
        } else {
            Toast.makeText(VitamioVideoPlayer.this, "大哥，你没有视频呀！让我咋播！", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void getData() {
        //得到播放地址
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void initData() {
        utils = new Utils();
        //注册电量广播
        receiver = new MyReceiver();
        IntentFilter intentFiler = new IntentFilter();
        //当电量发生变化的时候发这个广播
        intentFiler.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFiler);

        //2、实例化手势识别器，并且重写单击、双击、长按手势方法
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {  //双击全屏切换
//                Toast.makeText(SystemVideoPlayer.this, "我被双击了", Toast.LENGTH_SHORT).show();
                setFullScreenAndDefault();

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isshowMediaController) {
                    hideMediaController();//隐藏控制面板

                    //把隐藏消息移除
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    showMediaController();//显示控制面板
                    //发消息4s后隐藏
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        //获取屏幕的宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            //默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            //全屏
            setVideoType(FULL_SCREEN);
        }
    }
class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
    //当底层解码准备好的时候
    @Override
    public void onPrepared(MediaPlayer mp) {
        //视频的宽高
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        videoview.start();//开始播放
        //得到视频的总时长,关联总长度
        int duration = (int) videoview.getDuration();
        seekbarVideo.setMax(duration);
        tvDuration.setText(utils.stringForTime(duration));

        hideMediaController();//默认隐藏控制面板
        //发消息
        handler.sendEmptyMessage(PROGRESS);

        //把加载页面消失掉
        ll_loading.setVisibility(View.GONE);

        //拖动完成
//            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                @Override
//                public void onSeekComplete(MediaPlayer mp) {
//                    Toast.makeText(SystemVideoPlayer.this, "拖动完成", Toast.LENGTH_SHORT).show();
//                }
//            });

//            videoview.setVideoSize(mp.getVideoWidth(),mp.getVideoHeight());
        //屏幕的默认播放
        setVideoType(FULL_SCREEN);
    }
}

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN://全屏
                //1、设置视频换面的大小 -->屏幕有多大就是多大
                videoview.setVideoSize(screenWidth, screenHeight);
                //2、设置按钮的转态 -- 默认
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN://默认
                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                // for compatibility, we adjust size based on aspect ratio
                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }

                videoview.setVideoSize(width,height);
                //2.设置按钮的状态--全屏
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//0到100
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    //监听事件
    private void setListener() {
        //准备好的监听
        videoview.setOnPreparedListener(new MyOnPreparedListener());

        //播放出错的监听
        videoview.setOnErrorListener(new MyOnErrorListener());

        //播放完成的监听
        videoview.setOnCompletionListener(new MyOnCompletionListener());

        //设置seekbar状态变化的监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            //监听视频播放卡
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoview.setOnInfoListener(new MyOnInfoListener());
            }
        }

    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: //视频卡了，拖动卡
                    ll_buffer.setVisibility(View.VISIBLE);//显示
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:  //视频卡结束了，拖动卡结束了
                    ll_buffer.setVisibility(View.GONE);    //隐藏
                    break;
            }
            return false;
        }
    }


    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, isMute);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 设置音量的大小
     */
    private void updataVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }
    }

    //设置时间
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        //当手指滑动的时候，会引起seekBar进度变化，会回调这个方法
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }
        }

        //当手指触碰的时候回调这个方法
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        //当手指离开的时候回调这个方法
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }



    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
//            Toast.makeText(SystemVideoPlayer.this, "播放出错了哦", Toast.LENGTH_SHORT).show();
            //当视频格式不支持时，自动调用万能播放器
            showErrorDialog();
            return true;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("抱歉，无法播放该视频！！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }



    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (position != mediaItems.size() - 1) {
                videoview.pause();
                Toast.makeText(VitamioVideoPlayer.this, "5秒后自动播放下个视频", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playNextVideo();
                    }
                }, 5000);
            } else {
                videoview.pause();
                Toast.makeText(VitamioVideoPlayer.this, "没有下一个视频了，5秒后自动返回到主页面", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 5000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        //移除消息
        handler.removeCallbacksAndMessages(null);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    private float startY;
    //屏幕的高
    private float touchRang;
    //当一按下的音量
    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        //把事件传递给手势识别器
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  //手指按下
                //1.按下记录值
                startY = event.getY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:  //手指移动
                //2、移动的记录相关值
                float endY = event.getY();
                float distanceY = startY - endY;
                //改变声音 = （滑动屏幕的距离 ： 总距离）* 音量最大值
                float delta = (distanceY / touchRang) * maxVoice;
                //最终是声音 = 原来声音 ＋ 改变的声音
                int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                if (delta != 0) {
                    isMute = false;
                    updataVoice(voice, isMute);
                }
                break;
            case MotionEvent.ACTION_UP:   //手指离开
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
        }
        return super.onTouchEvent(event);
    }

    //显示控制面板
    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    //隐藏控制面板
    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isshowMediaController = false;
    }

    @Override
    public boolean onKeyDown(int keyCodey, KeyEvent event) {
        if (keyCodey == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        } else if (keyCodey == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCodey, event);
    }
}
