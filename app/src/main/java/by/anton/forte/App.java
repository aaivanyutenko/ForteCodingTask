package by.anton.forte;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.SparseArray;

public class App extends Application {
    /**
     * Using memory cache for reuse received bitmaps during app lifecycle
     */
    SparseArray<Bitmap> cache = new SparseArray<>();
}
