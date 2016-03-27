package by.anton.forte;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader extends AppCompatActivity {
    private static final String TAG = Main.class.getName();
    private static volatile boolean isViewReady;
    private static volatile boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    protected synchronized void download(final ImageView image, final ProgressBar progress, final String link, final boolean fitHeight) {
        if (image == null || progress == null || link == null) {
            return;
        }
        if (!isConnected) {
            progress.setVisibility(View.GONE);
            return;
        }
        if (!isViewReady) {
            image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    isViewReady = true;
                    image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.i(TAG, "w: " + image.getMeasuredWidth() + ", h: " + image.getMeasuredHeight());
                if (isViewReady && image.getDrawable() == null) {
                    progress.setVisibility(View.VISIBLE);
                    image.getViewTreeObserver().removeOnPreDrawListener(this);
                    final int measuredWidth = image.getMeasuredWidth();
                    final int key = link.hashCode() + measuredWidth;
                    Bitmap bitmap = ((App) getApplication()).cache.get(key);
                    if (bitmap != null) {
                        apply(image, progress, bitmap, link, null);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(link);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    final Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                                    int width = measuredWidth;
                                    int height = Math.round(bitmap.getHeight() * (((float) width) / bitmap.getWidth()));
                                    if (fitHeight) {
                                        height = image.getMeasuredHeight();
                                        width = Math.round(bitmap.getWidth() * (((float) height) / bitmap.getHeight()));
                                    }
                                    final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
                                    bitmap.recycle();
                                    ((App) getApplication()).cache.put(key, scaled);
                                    apply(image, progress, scaled, link, null);
                                } catch (IOException e) {
                                    apply(null, progress, null, link, e);
                                }
                            }
                        }).start();
                    }
                }
                return true;
            }
        });
    }

    private void apply(final ImageView image, final ProgressBar progress, final Bitmap bitmap, final String link, final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e == null) {
                    image.setTag(link);
                    image.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(ImageDownloader.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                progress.setVisibility(View.GONE);
            }
        });
    }
}
