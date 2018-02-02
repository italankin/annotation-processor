package com.italankin.mvp.example.ui.main.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.flickr.FlickrUtils;
import com.italankin.flickr.bean.Photo;
import com.italankin.mvp.example.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static Intent getStartIntent(Context context, Photo photo) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_OWNER, photo.ownerName);
        intent.putExtra(EXTRA_OWNER_URL, FlickrUtils.getProfileUrl(photo.owner));
        intent.putExtra(EXTRA_TITLE, photo.title);
        intent.putExtra(EXTRA_URL, photo.urlLarge);
        intent.putExtra(EXTRA_WEB_URL, FlickrUtils.getPhotoWebUrl(photo));
        return intent;
    }

    ///////////////////////////////////////////////////////////////////////////
    // DetailActivity
    ///////////////////////////////////////////////////////////////////////////

    private static final String EXTRA_OWNER = "owner";
    private static final String EXTRA_OWNER_URL = "owner_url";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_URL = "url";
    private static final String EXTRA_WEB_URL = "web_url";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.image)
    ImageView image;

    @BindView(R.id.owner)
    TextView owner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        toolbar.setTitle(intent.getStringExtra(EXTRA_TITLE));
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_overflow));
        owner.setText(intent.getStringExtra(EXTRA_OWNER));
        owner.setOnClickListener(v -> {
            openUrl(getIntent().getStringExtra(EXTRA_OWNER_URL));
        });
        Picasso.with(this)
                .load(intent.getStringExtra(EXTRA_URL))
                .placeholder(R.drawable.ic_placeholder_photo)
                .into(image);

        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_on_web:
                openUrl(getIntent().getStringExtra(EXTRA_WEB_URL));
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
