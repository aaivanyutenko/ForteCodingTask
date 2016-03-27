package by.anton.forte;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Base extends AppCompatActivity {
    private static final String TAG = Main.class.getName();
    private static volatile boolean isViewReady;
    private static volatile boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    protected void download(final ImageView image, final ProgressBar progress, final String link, final boolean fitHeight) {
        if (image == null || progress == null || link == null) {
            return;
        }
        if (!isConnected) {
            progress.setVisibility(View.GONE);
            return;
        }
        if (!isViewReady) {
            /**
             * Global layout listener need for make sure that pre-draw listener will work with proper size
             */
            image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    isViewReady = true;
                    image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        /**
         * Pre-draw listener used for get real image view dimensions
         */
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.i(TAG, "w: " + image.getMeasuredWidth() + ", h: " + image.getMeasuredHeight());
                if (isViewReady && image.getDrawable() == null) {
                    image.getViewTreeObserver().removeOnPreDrawListener(this);
                    progress.setVisibility(View.VISIBLE);
                    ImageProcessing imageProcessing = new ImageProcessing(link, fitHeight, image, progress, Base.this);
                    imageProcessing.execute();
                }
                return true;
            }
        });
    }
}
