package by.anton.forte;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.SparseArray;

public class App extends Application {
    SparseArray<Bitmap> cache = new SparseArray<>();
}
