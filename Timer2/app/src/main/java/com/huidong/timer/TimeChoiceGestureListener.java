package com.huidong.timer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import java.util.LinkedList;

public class TimeChoiceGestureListener extends GestureDetector.SimpleOnGestureListener {

    void delete(View v)
    {
        TextView textView = (TextView)v;
        ViewParent viewParent = v.getParent();
        if (viewParent instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup)viewParent;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                View current = (View)viewGroup.getChildAt(i);
                if (current instanceof TextView)
                {
                    TextView tCurrent = (TextView) current;
                    if (tCurrent.getText().equals(textView.getText()))
                    {
                        viewGroup.removeView(current);
                        isDelete = true;
                        String save = new String();
                        for (int k = 1; k < viewGroup.getChildCount(); ++k)
                        {
                            if (viewGroup.getChildAt(k) instanceof TextView)
                            {
                                save += ((TextView)viewGroup.getChildAt(k)).getText().toString() + ";";
                            }
                        }
                        SharedPreferences sp = context.getSharedPreferences("config", context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("timeList", save);
                        editor.commit();
                    }
                }
            }
        }
    }

    android.content.DialogInterface.OnClickListener clickListener = new android.content.DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case -1: // 是
                    delete(deleteView);
                    break;
                case -2: // 否
                    break;
            }
        }
    };

    @Override
    public boolean onDown(MotionEvent e) {
        event = 1;
        return super.onDown(e);
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }
    @Override
    public void onLongPress(MotionEvent e)
    {
        super.onLongPress(e);
        TextView textView = (TextView)view;
        ViewParent viewParent = view.getParent();

        // 先赋值，防止被覆盖，导致删错
        deleteView = view;
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("确认删除吗")
                .setPositiveButton("是" , clickListener)
                .setNegativeButton("否" , clickListener)
                .show();
    }
    public View deleteView;
    public View view;
    public Context context;
    public boolean isDelete = false;
    public int event = 0;
}
