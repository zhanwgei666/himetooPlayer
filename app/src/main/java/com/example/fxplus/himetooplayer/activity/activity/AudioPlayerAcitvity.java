package com.example.fxplus.himetooplayer.activity.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxplus.himetooplayer.IMusicPlayerService;
import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.domain.Lyric;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;
import com.example.fxplus.himetooplayer.activity.service.MusicPlayerService;
import com.example.fxplus.himetooplayer.activity.util.LyricUtils;
import com.example.fxplus.himetooplayer.activity.util.Utils;
import com.example.fxplus.himetooplayer.activity.view.BaseVisualizerView;
import com.example.fxplus.himetooplayer.activity.view.ShowLyrivView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import io.vov.vitamio.EGL;

public class AudioPlayerAcitvity<showLyricView> extends Activity implements View.OnClickListener {
    private int position;//拿到位置
    private IMusicPlayerService service;//服务的代理类，通过他可以调用服务的方法
    private MyReceiver receiver;


    /**
     * true:从状态栏进入，不需要重新播放
     * false：从播放列表进入
     */
    private boolean notification;

    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyrivView showLyricView;
    private BaseVisualizerView baseVisualizerView;
    /**
     * 进度更新
     */
    private static final int PROGRESS = 1;
    /**
     * 显示歌词
     */
    private static final int SHOW_LYRIC = 2;

    private Utils utils;
//    private EGL mVisualizer;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-03-01 11:01:11 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_audioplayer);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketanimation = (AnimationDrawable) ivIcon.getBackground();
        rocketanimation.start();
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrc = (Button) findViewById(R.id.btn_lyrc);
        showLyricView = (ShowLyrivView) findViewById(R.id.showLyricView) ;
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);

        //设置音频的拖动
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                //拖动进度
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            setPlaymode();
        } else if (v == btnAudioPre) {
            // Handle clicks for btnAudioPre
            if(service != null){
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {
            // Handle clicks for btnAudioStartPause
            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        //暂停
                        service.pause();
                        //按钮--播放
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                    } else {
                        //播放
                        service.start();
                        //按钮--暂停
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioNext) {
            // Handle clicks for btnAudioNext
            if(service != null){
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
        }
    }

    private void setPlaymode() {
        try {
            int playmode = service.getPlayMode();
            if(playmode==MusicPlayerService.REPEAT_NORMAL){
                playmode = MusicPlayerService.REPEAT_SINGLE;
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                playmode = MusicPlayerService.REPEAT_ALL;
            }else if(playmode ==MusicPlayerService.REPEAT_ALL){
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }else{
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }

            //保持
            service.setPlayMode(playmode);

            //设置图片
            showPlaymode();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = service.getPlayMode();

            if(playmode==MusicPlayerService.REPEAT_NORMAL){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerAcitvity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                Toast.makeText(AudioPlayerAcitvity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            }else if(playmode ==MusicPlayerService.REPEAT_ALL){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                Toast.makeText(AudioPlayerAcitvity.this, "全部循环", Toast.LENGTH_SHORT).show();
            }else{
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerAcitvity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 校验状态
     */
    private void checkPlaymode() {
        try {
            int playmode = service.getPlayMode();

            if(playmode==MusicPlayerService.REPEAT_NORMAL){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            }else if(playmode ==MusicPlayerService.REPEAT_ALL){
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            }else{
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

            //校验播放和暂停的按钮
            if(service.isPlaying()){
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            }else{
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC://显示歌词

                    //1.得到当前的进度
                    try {
                        int currentPosition = service.getCurrentPosition();


                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句

                        showLyricView.setshowNextLyric(currentPosition);
                        //3.实时的发消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        //1、得到当前进度
                        int currentPosition = service.getCurrentPosition();
                        //2、设置seekBar.setProgress(进度)
                        seekbarAudio.setProgress(currentPosition);
                        //3、时间进度更新
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));//当前时间/总时间
                        //4、每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();//注册广播
        findViews();
        getData();
        binAndStarService();//绑定服务并且启动服务
    }

    private void initData() {
        utils = new Utils();
//        //注册广播
//        receiver = new MyReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
//        registerReceiver(receiver, intentFilter);
        //1、EventBus注册
        EventBus.getDefault().register(this);//this为当前类
    }

    private ServiceConnection con = new ServiceConnection() {
        /**
         * 当链接成功的时候回调这个方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!notification){
                        service.openAudio(position);
                    }else {
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候回调这个方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);

        }
    }

    //3、EventBus订阅方法
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem) {
        //发消息开始歌词同步
        ShowLyric(SHOW_LYRIC);
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }

    private Visualizer mVisualizer;
    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi()
    {
        try {
            int audioSessionid = service.getAudioSessionId();
            System.out.println("audioSessionid=="+audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void ShowLyric(int showLyric) {
        //解析歌词
        LyricUtils lyricUtils = new LyricUtils();

        try {
            String path = service.getAudioPath();//得到歌曲的绝对路径

            //传歌词文件
            //mnt/sdcard/audio/beijingbeijing.mp3
            //mnt/sdcard/audio/beijingbeijing.lrc
            path = path.substring(0,path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if(!file.exists()){
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);//解析歌词

            showLyricView.setLyrics(lyricUtils.getLyrics());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (lyricUtils.isExistsLyric()){
            handler.sendEmptyMessage(showLyric);
        }
    }

    private void showViewData() {
        try {
            tvArtist.setText(service.getArtist());//拿到作者并显示
            tvName.setText(service.getName());//拿到歌曲名并显示

            //设置进度条的最大值
            seekbarAudio.setMax(service.getDuration());
            //发消息
            handler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绑定服务并且启动服务
     */
    private void binAndStarService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.mitu.mobileplayer_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);//不会多次调用不会启动多次服务
    }

    /**
     * 得到数据
     */
    private void getData() {
        notification = getIntent().getBooleanExtra("Notification" ,false);
        if(!notification){
            position = getIntent().getIntExtra("position", 0);
        }
    }

    @Override
    protected void onDestroy() {
        //移除消息
        handler.removeCallbacksAndMessages(null);
        //取消广播
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//            receiver = null;
//        }
        //2、EventBus取消注册
        EventBus.getDefault().unregister(this);

        //解绑服务
        if(con != null){
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVisualizer != null){
            mVisualizer.release();
        }
    }
}
