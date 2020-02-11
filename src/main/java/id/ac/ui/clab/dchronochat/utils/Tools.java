package id.ac.ui.clab.dchronochat.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

public class Tools {

    public static Bitmap getVideoPhoto(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        try {
            media.setDataSource(videoPath);
            return media.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getImageThumbnail(Context context, ContentResolver cr, String Imagepath) {
        ContentResolver testcr = context.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,};
        String whereClause = MediaStore.Images.Media.DATA + " = '" + Imagepath + "'";
        Cursor cursor = testcr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, whereClause, null, null);
        int _id = 0;
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else if (cursor.moveToFirst()) {
            int _idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int _dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                _id = cursor.getInt(_idColumn);
            } while (cursor.moveToNext());
        }
        cursor.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return MediaStore.Images.Thumbnails.getThumbnail(cr,
                _id, MediaStore.Images.Thumbnails.MINI_KIND, options);
    }

    public static String convertB2MB(int length) {
        return convertB2KB(length / 1024);
    }

    public static String convertB2KB(int length) {
        return String.valueOf(length / 1024);
    }

    public static String convertB2KB(String length) {
        int len = Integer.valueOf(length);
        return convertB2KB(len);
    }

}
