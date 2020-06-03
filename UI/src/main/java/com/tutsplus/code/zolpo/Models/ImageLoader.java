package com.tutsplus.code.zolpo.Models;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImageLoader {

    public static class StaticImageLoader{

        public StaticImageLoader() {
        }
        private static final String TAG = "StaticImageLoader";

        public static void LoadImageFromUrl(Context iContext, String iUrl, ImageView iImageView, int iErrorImage) {
            Log.d(TAG,"LoadImageFromUrl: loading image from url : " + iUrl);
            GlideApp.with(iContext)
                    .load(iUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(iErrorImage)             // if url not valid
                    .into(iImageView);
        }
    }
}
