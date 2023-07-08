package com.android.ppp.ui.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.android.ppp.R;

public class CustomProgressDialog extends ProgressDialog
{
    private Context context;
    public CustomProgressDialog(Context context)
    {
        super(context);
        this.context = context;
    }

    public CustomProgressDialog(Context context, int theme)
    {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View view = View.inflate(getContext(), R.layout.customprogressdialog, null);
        ImageView progress_img = (ImageView)view. findViewById(R.id.iv_bg);
        //在正中心，在1秒内，从0度转到360度，不停止
        Animation operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_upload_progress);
        progress_img.setAnimation(operatingAnim);
        setContentView(view);
    }

    @Override
    public void show()
    {
        super.show();
    }

    @Override
    public void hide()
    {
        super.hide();
    }
}