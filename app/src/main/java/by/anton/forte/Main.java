package by.anton.forte;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Main extends ImageDownloader {
    private static final String SELECTED = "SELECTED";
    private static final boolean FIT_HEIGHT = false;
    private SparseArray<View> group = new SparseArray<>();
    private Integer selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ImageView first = (ImageView) findViewById(R.id.first);
        ImageView second = (ImageView) findViewById(R.id.second);
        group.put(R.id.first, first);
        group.put(R.id.second, second);
        ProgressBar progressFirst = (ProgressBar) findViewById(R.id.progress_first);
        ProgressBar progressSecond = (ProgressBar) findViewById(R.id.progress_second);
        download(first, progressFirst, "http://heartofgreen.typepad.com/.a/6a00d83451cedf69e201a73dcaba0a970d-pi", FIT_HEIGHT);
        download(second, progressSecond, "http://images5.fanpop.com/image/photos/27900000/Ocean-Animals-animals-27960311-1920-1200.jpg", FIT_HEIGHT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (selected != null) {
            outState.putInt(SELECTED, selected);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        selected = (Integer) savedInstanceState.get(SELECTED);
        if (selected != null) {
            onSelect(group.get(selected));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void onFab(View view) {
        if (selected == null || group.get(selected) == null) {
            Snackbar.make(view, "Please, select some image", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        } else {
            Intent intent = new Intent(this, Details.class);
            String url = (String) group.get(selected).getTag();
            intent.putExtra(Details.URL, url);
            startActivity(intent);
        }
    }

    public void onSelect(View view) {
        if (selected == null) {
            selected = view.getId();
            view.setBackgroundResource(R.drawable.frame);
        } else {
            if (selected != view.getId()) {
                group.get(selected).setBackground(null);
                selected = view.getId();
                view.setBackgroundResource(R.drawable.frame);
            } else if (view.getBackground() == null) {
                view.setBackgroundResource(R.drawable.frame);
            }
        }
    }
}
