package com.example.fxplus.himetooplayer.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.fxplus.himetooplayer.activity.domain.Lyric;

import com.example.fxplus.himetooplayer.activity.util.DensityUtil;

import java.util.ArrayList;

/**
 * 自定义歌词显示控件
 */
public class ShowLyrivView extends TextView {

    //歌词列表
    private ArrayList<Lyric> lyrics;

    //创建画笔
    private Paint paint;
    private Paint whitepaint;//白色画笔

    //控件的宽高
    private int width;
    private int height;

    //歌词列表中的索引，代表第几句歌词
    private int index;

    //每行的高
    private int textHeight ;
    //当前播放进度
    private int crrentPosition;
    //高亮显示的时间或者是休眠的时间
    private long sleepTime;
    //时间戳，什么时刻到高亮哪句歌词
    private long timePoint;

    /**
     * 设置歌词列表
     * @param lyrics
     */
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    public ShowLyrivView(Context context) {
        this(context,null);
    }

    public ShowLyrivView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShowLyrivView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    //控件的宽高
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }
    private void initView(Context context) {

        textHeight = (int) DensityUtil.dip2px(context,20);//对应的像素
        //创建画笔
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(DensityUtil.dip2px(context,20));
//        paint.setAntiAlias(true);
        //设置居中对齐
        paint.setTextAlign(Paint.Align.CENTER);

        whitepaint = new Paint();
        whitepaint.setColor(Color.WHITE);
        whitepaint.setTextSize(DensityUtil.dip2px(context,20));
        whitepaint.setAntiAlias(true);
        //设置居中对齐
        whitepaint.setTextAlign(Paint.Align.CENTER);

//        lyrics = new ArrayList<>();
//
//        Lyric lyric = new Lyric();
//        for(int i=0 ; i<1000;i++){
//
//            lyric.setTimePoint(1000 * i);
//            lyric.setSleepTime(1500 + i);
//            lyric.setContent(i + "aaaaaaaaaaaaaaa" + i);
//            //把歌词添加到集合中
//            lyrics.add(lyric);
//            lyric = new Lyric();
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(lyrics != null && lyrics.size() >0){
            //绘制歌词:绘制当前句
            String currentText = lyrics.get(index).getContent();
            canvas.drawText(currentText,width/2,height/2,paint);
            // 绘制前面部分
            int tempY = height/2;//Y轴的中间坐标
            for(int i= index-1;i >= 0 ;i--){
                //每一句歌词
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if(tempY < 0){
                    break;
                }
                canvas.drawText(preContent,width/2,tempY,whitepaint);
            }

            // 绘制后面部分
            tempY = height/2;//Y轴的中间坐标
            for(int i= index+1; i < lyrics.size() ;i++){
                //每一句歌词
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if(tempY > height){
                    break;
                }
                canvas.drawText(nextContent,width/2,tempY,whitepaint);
            }
        }else {
            canvas.drawText("没有歌词",width/2,height/2,paint);
        }
    }

    //根据当前播放的位置，找出该高亮显示哪句歌词
    public void setshowNextLyric(int crrentPosition){
        this.crrentPosition = crrentPosition;
        if (lyrics == null || lyrics.size() == 0){
            return;
        }
        for (int i= 1; i<lyrics.size();i++){
            if (crrentPosition <lyrics.get(i).getTimePoint()){
                int tempIndex = i - 1;
                if (crrentPosition >= lyrics.get(tempIndex).getTimePoint()){
                    //当前正在播放的哪句歌词
                    index = tempIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    timePoint = lyrics.get(index).getTimePoint();
                }
            }
        }
        //重新绘制
        invalidate();//在主线程中
    }


}
