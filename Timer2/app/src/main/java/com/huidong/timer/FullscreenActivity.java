package com.huidong.timer;

import android.Manifest;
import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huidong.timer.databinding.ActivityFullscreenBinding;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

public class FullscreenActivity extends AppCompatActivity {

    private View mAllScreenView; // 整个屏幕view

    private LinearLayout mScrollViewLay;

    private Date mDateStart;

    GregorianCalendar time;

    private int mOriCalcTimeSec;

    private int mCalcTimeSec;

    private ScrollView mTimeChoiceView; // 整个屏幕view

    private GestureDetector mTimeChoiceGesture;
    
    private TimeChoiceGestureListener mTimeChoiceGestureListener;

    // 分钟显示veiw
    private TextView mMinuteView;
    // 秒种view
    private TextView mSecView;

    private ImageButton imgButtonControl;

    // 音乐播放组件
    private static MediaPlayer mediaplayer = null;

    private boolean isPlay = false;

    private Handler mhandle = new Handler();

    private volatile boolean isPause = true;//是否暂停

    TextView mInputTimeView;

    SharedPreferences sp;

    // 开始结束按钮监听
    private View.OnClickListener mStartListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isPause == true)
            {
                isPause = false;
                // 超时后设置文字为红色
                mMinuteView.setTextColor(0xffffffff);
                mSecView.setTextColor(0xffffffff);
                mOriCalcTimeSec = mCalcTimeSec = Integer.valueOf((String) mMinuteView.getText()) * 60 + Integer.valueOf((String) mSecView.getText());
                mDateStart = new GregorianCalendar().getTime();
                mhandle.postDelayed(timeRunable, 10);
                imgButtonControl.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                if (isPlay)
                {
                    isPlay = false;
                    mediaplayer.stop();
                    mediaplayer.prepareAsync();
                }
                isPause = true;
                imgButtonControl.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    };

    private View.OnClickListener mInputTimeOk = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int timeSec = Integer.valueOf(mInputTimeView.getText().toString());

            @SuppressLint("DefaultLocale") String min = String.format("%02d", timeSec / 60);
            @SuppressLint("DefaultLocale") String sec = String.format("%02d", timeSec % 60);
            String prepareTimeSave = min + ":" + sec;

            String timeList = sp.getString("timeList", ""); // 第二个参数为默认值

            if (timeList.contains(prepareTimeSave))
            {
                return;
            }

            addTimeChoice(FullscreenActivity.this, mScrollViewLay, prepareTimeSave, mTimeChoiceListen);
            String save = new String();
            ViewGroup viewGroup = (ViewGroup)mScrollViewLay;
            for (int i = 1; i < viewGroup.getChildCount(); ++i)
            {
                if (viewGroup.getChildAt(i) instanceof TextView)
                {
                    save += ((TextView)viewGroup.getChildAt(i)).getText().toString() + ";";
                }
            }

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("timeList", save);
            editor.commit();
        }
    };
    // 菜单按钮监听
    private View.OnClickListener mMenuListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mScrollViewLay.removeAllViews();
            ViewGroup t = (ViewGroup) View.inflate(FullscreenActivity.this, R.layout.add_time_lay, null);
            mInputTimeView = (TextView)t.getChildAt(0);
            View mbOk = t.getChildAt(1);
            mbOk.setOnClickListener(mInputTimeOk);
            mScrollViewLay.addView(t);

            String save = sp.getString("timeList", ""); // 第二个参数为默认值
            String[] list = save.split(";");

            for (int i = 0; i < list.length; ++i)
            {
                if (list[i] != null && !list[i].isEmpty())
                {
                    addTimeChoice(FullscreenActivity.this, mScrollViewLay, list[i], mTimeChoiceListen);
                }
            }
            mTimeChoiceView.setVisibility(View.VISIBLE);
        }
    };

    private void addTimeChoice(Context context,ViewGroup parent, String str, View.OnClickListener listener)
    {
        TextView t = (TextView) View.inflate(context, R.layout.time_list_show_view, null);
        t.setText(str);
        t.setOnClickListener(listener);
        t.setOnTouchListener(mTimeChoiceTouchListen);
        parent.addView(t, 1);
    }

    // 点击设置时间
    private View.OnClickListener mTimeChoiceListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isPause)
            {
                String strTime = ((TextView)v).getText().toString();
                String [] strTimes = strTime.split(":");
                mMinuteView.setText(strTimes[0]);
                mSecView.setText(strTimes[1]);
            }
        }
    };

    private View.OnTouchListener mTimeChoiceTouchListen = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTimeChoiceGestureListener.view = view;
            mTimeChoiceGesture.onTouchEvent(motionEvent);
            if (mTimeChoiceGestureListener.event == 1)
            {
                return false;
            }
            return true;
        }
    };

    // 调用系统文件夹
    private View.OnClickListener mMusicListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            FullscreenActivity.this.requestPermissions(new String[]{
                    Manifest.permission.WRITE_SETTINGS
            }, 1);

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");

            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 100);

        }
    };

    // 重置时间
    private View.OnClickListener mResetTimeListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isPause = true;
            @SuppressLint("DefaultLocale") String min = String.format("%02d",mOriCalcTimeSec / 60);
            @SuppressLint("DefaultLocale") String sec = String.format("%02d",mOriCalcTimeSec % 60);
            mMinuteView.setText(min);
            mSecView.setText(sec);
            mMinuteView.setTextColor(0xffffffff);
            mSecView.setTextColor(0xffffffff);
            imgButtonControl.setImageResource(android.R.drawable.ic_media_play);
            if (isPlay)
            {
                isPlay = false;
                mediaplayer.stop();
                mediaplayer.prepareAsync();
            }
        }
    };

    private Uri uri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("test", "onActivityResult: " + requestCode + " resultCode" + resultCode + " null data = " + (data == null));
        if (data != null) {
            // 如果选择了文件，resultCode = -1
            if (requestCode == 100) {

                uri = data.getData();
                if (mediaplayer != null)
                {
                    mediaplayer.release();
                }
                try {
                    mediaplayer = MediaPlayer.create(this, uri);
                } catch (Exception e) {
                    Toast.makeText(FullscreenActivity.this,"音乐文件类型不支持",Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("file", "onActivityResult: " + uri.toString() + "path" + uri.getPath());

                String filePathUriUtils = UriUtils.getFilePathFromURI(FullscreenActivity.this, uri);

                // 删除原来的文件
                String oriMusicPath = sp.getString("music", "");
                if (!oriMusicPath.isEmpty())
                {
                    File file = new File(oriMusicPath);
                    if (file.exists())
                    {
                        file.delete();
                    }
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("music", filePathUriUtils);
                editor.commit();
            }
        } else {
            if (requestCode == 100) {
                // 如果未选择文件 resultCode = 0
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("music", "");
                editor.commit();
                mediaplayer = MediaPlayer.create(FullscreenActivity.this, R.raw.bell);
            }
        }
    }

    private View.OnTouchListener mAllTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // 触摸时间选择以外的区域关闭时间选择View
            mTimeChoiceView.setVisibility(View.INVISIBLE);
            return true;
        }
    };

    private ActivityFullscreenBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAllScreenView = binding.allScreen;
        mMinuteView = (TextView)binding.textView;
        mSecView = (TextView)binding.textView2;

        try {
            String music = sp.getString("music", ""); // 第二个参数为默认值

            if (music == null || music.isEmpty())
            {
                mediaplayer = MediaPlayer.create(FullscreenActivity.this, R.raw.bell);
            } else {
                try {
                    // 使用设置里的值
                    mediaplayer = MediaPlayer.create(FullscreenActivity.this, Uri.parse(music));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 创建失败使用默认
            if (mediaplayer == null)
            {
                mediaplayer = MediaPlayer.create(FullscreenActivity.this, R.raw.bell);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        imgButtonControl = binding.imageButton2;
        mAllScreenView.setClickable(true);
        mAllScreenView.setOnTouchListener(mAllTouch);
        mScrollViewLay = binding.scrollViewLay;
        binding.imageButton2.setOnClickListener(mStartListen);
        mTimeChoiceView = binding.scrollView;
        mTimeChoiceView.bringToFront();
        mTimeChoiceView.setVisibility(View.INVISIBLE);

        time = new GregorianCalendar();
        binding.imageButton.setOnClickListener(mMenuListen);
        binding.chooseMusic.setOnClickListener(mMusicListen);
        binding.resetTime.setOnClickListener(mResetTimeListen);
        mOriCalcTimeSec = mCalcTimeSec = Integer.valueOf((String) mMinuteView.getText()) * 60 + Integer.valueOf((String) mSecView.getText());

        // 初始化手势判断
        mTimeChoiceGestureListener = new TimeChoiceGestureListener();
        mTimeChoiceGestureListener.context = FullscreenActivity.this;
        mTimeChoiceGesture = new GestureDetector(this, mTimeChoiceGestureListener);
        sp = getSharedPreferences("config", FullscreenActivity.this.MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private Runnable timeRunable = new Runnable() {
        @Override
        public void run() {
            if (isPause) return;
            Date dateNow = new GregorianCalendar().getTime();
            long mill = dateNow.getTime() - mDateStart.getTime();
            mill =  mCalcTimeSec - mill / 1000;//剩余秒数

            if (mill <= 0)
            {
                if ((!isPlay || mill % 60 == 0) && !mediaplayer.isPlaying())
                {

                    Log.i("play", "play start");
                    isPlay = true;

                    mediaplayer.setLooping(false);
                    mediaplayer.seekTo(0);
                    mediaplayer.start();

                    mMinuteView.setTextColor(0xffff0000);
                    mSecView.setTextColor(0xffff0000);
                }
                mill = -mill;
            }

            int s= (int) (mill%60);//秒
            int m= (int) (mill/60);//分
            @SuppressLint("DefaultLocale") String min = String.format("%02d",m);
            @SuppressLint("DefaultLocale") String sec = String.format("%02d",s);
            mMinuteView.setText(min);
            mSecView.setText(sec);

            if (!isPause) {
                //递归调用本runable对象，实现循环执行任务
                mhandle.postDelayed(this, 100);
            }
        }
    };

}