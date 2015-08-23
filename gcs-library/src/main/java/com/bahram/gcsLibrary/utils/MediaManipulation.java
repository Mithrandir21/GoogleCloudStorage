package com.bahram.gcsLibrary.utils;

import android.graphics.Bitmap;

/**
 * Created by bahram on 14.03.2015.
 */
public class MediaManipulation
{
    // TODO - Add resizing functions

    public enum SupportedImageFormats
    {
        png, jpg, webp
    }


    public static Bitmap.CompressFormat getCompressFormat(SupportedImageFormats format)
    {
        if( format != null )
        {
            switch( format )
            {
                case png:
                {
                    return Bitmap.CompressFormat.PNG;
                }
                case webp:
                {
                    return Bitmap.CompressFormat.WEBP;
                }
                default:
                {
                    return Bitmap.CompressFormat.JPEG;
                }
            }
        }

        return null;
    }
}

