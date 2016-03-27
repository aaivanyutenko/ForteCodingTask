package by.anton.forte;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Details extends ImageDownloader {
    public static final String URL = "URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        ImageView details = (ImageView) findViewById(R.id.details);
        ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
        int r = getWindowManager().getDefaultDisplay().getRotation();
        boolean fitHeight = false;
        if (r == Surface.ROTATION_90 || r == Surface.ROTATION_270) {
            fitHeight = true;
        }
        download(details, progress, getIntent().getStringExtra(URL), fitHeight);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
