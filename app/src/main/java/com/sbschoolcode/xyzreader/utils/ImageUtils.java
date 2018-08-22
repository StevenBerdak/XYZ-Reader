package com.sbschoolcode.xyzreader.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

public class ImageUtils {

    public static void seamlessLoadFromUrlToContainer(ImageView imageView, String urlString, View topLevelContainer){
        Glide.with(imageView)
                .asBitmap()
                .load(urlString)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(bitmap);

                        topLevelContainer.setVisibility(View.VISIBLE);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                        alphaAnimation.setDuration(1000);
                        alphaAnimation.setRepeatMode(Animation.RESTART);
                        topLevelContainer.startAnimation(alphaAnimation);
                    }
                });
    }
}
