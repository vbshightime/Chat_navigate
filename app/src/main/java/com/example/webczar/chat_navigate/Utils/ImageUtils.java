package com.example.webczar.chat_navigate.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static android.R.attr.width;
import static android.support.v7.appcompat.R.attr.height;

/**
 * Created by webczar on 1/1/2018.
 */

public class ImageUtils {
    public static final int AVATAR_WIDTH = 128;
    public static final int AVATAR_HEIGHT = 128;

    public static Bitmap cropToSquare(Bitmap srcBmp){
        Bitmap dstBmp = null;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        }else{
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }


    public static InputStream convertBitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        return bs;
    }

    public static String encodeBase64(Bitmap liteImage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        liteImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);

    }

    public static Bitmap makeImageLite(InputStream is, int width, int height, int avatarWidth, int avatarHeight) {

        int inSampleSize = 1;

        if (height > avatarHeight || width > avatarWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= avatarHeight
                    && (halfWidth / inSampleSize) >= avatarWidth) {
                inSampleSize *= 2;
            }
        }

        // Calculate inSampleSize
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static RoundedBitmapDrawable getRoundedImage(Context context , Bitmap src) {
        Resources res = context.getResources();
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res,src);
        dr.setCornerRadius(Math.max(src.getWidth(),src.getHeight()/2.0f));
        return dr;
    }
}
