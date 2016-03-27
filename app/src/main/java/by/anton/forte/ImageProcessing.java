package by.anton.forte;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageProcessing implements Runnable {
    private String link;
    private boolean fitHeight;
    private ImageView image;
    private ProgressBar progress;
    private Activity activity;
    private Bitmap bitmap;
    private Exception exception;

    public ImageProcessing(String link, boolean fitHeight, ImageView image, ProgressBar progress, Activity activity) {
        this.link = link;
        this.fitHeight = fitHeight;
        this.image = image;
        this.progress = progress;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            /**
             * Since Android deprecates Apache Http client there are the only optimal
             * and Android recommended approach for download data - HttpURLConnection
             */
            if (bitmap == null) {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                ((App) activity.getApplication()).cache.put(link.hashCode(), bitmap);
            }
            int[] measure = measure();
            if (measure != null) {
                bitmap = Bitmap.createScaledBitmap(bitmap, measure[0], measure[1], false);
                ((App) activity.getApplication()).cache.put(scaledHash(measure[0], measure[1]), bitmap);
            }
        } catch (IOException e) {
            exception = e;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                apply();
            }
        });
    }

    private int scaledHash(int width, int height) {
        int hash = link.hashCode();
        hash = 31 * hash + width;
        hash = 31 * hash + height;
        return hash;
    }

    private int[] measure() {
        if (fitHeight ? bitmap.getHeight() > image.getMeasuredHeight() : bitmap.getWidth() > image.getMeasuredWidth()) {
            int[] measure = new int[2];
            measure[0] = image.getMeasuredWidth();
            measure[1] = Math.round(bitmap.getHeight() * (((float) measure[0]) / bitmap.getWidth()));
            /**
             * Fit height or width
             */
            if (fitHeight) {
                measure[1] = image.getMeasuredHeight();
                measure[0] = Math.round(bitmap.getWidth() * (((float) measure[1]) / bitmap.getHeight()));
            }
            return measure;
        } else {
            return null;
        }
    }

    public void execute() {
        bitmap = ((App) activity.getApplication()).cache.get(link.hashCode());
        if (bitmap != null) {
            int[] measure = measure();
            if (measure != null) {
                Bitmap scaled = ((App) activity.getApplication()).cache.get(scaledHash(measure[0], measure[1]));
                if (scaled == null) {
                    new Thread(this).start();
                } else {
                    bitmap = scaled;
                    apply();
                }
            } else {
                apply();
            }
        } else {
            /**
             * This article http://techtej.blogspot.com.by/2011/03/android-thread-constructspart-4.html
             * used for choose the optimal approach for download images
             */
            new Thread(this).start();
        }
    }

    private void apply() {
        if (exception == null) {
            if (bitmap.getHeight() < image.getMeasuredHeight() || bitmap.getWidth() < image.getMeasuredWidth()) {
                /**
                 * Wrapping bitmap smaller than image view
                 */
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                image.setLayoutParams(params);
            }
            /**
             * Saving link for pick up when image view will be selected
             */
            image.setTag(link);
            image.setImageBitmap(bitmap);
        } else {
            /**
             * Simple error handling
             */
            Toast.makeText(activity, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        progress.setVisibility(View.GONE);
    }
}
