package duopoints.com.gcs.utils;

import android.graphics.Bitmap;

public class MediaManipulation {

    public enum SupportedImageFormats {
        png, jpg, webp
    }


    public static Bitmap.CompressFormat getCompressFormat(SupportedImageFormats format) {
        if (format != null) {
            switch ( format ) {
                case png: {
                    return Bitmap.CompressFormat.PNG;
                }
                case webp: {
                    return Bitmap.CompressFormat.WEBP;
                }
                default: {
                    return Bitmap.CompressFormat.JPEG;
                }
            }
        }

        return null;
    }
}

