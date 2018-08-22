package com.sbschoolcode.xyzreader.ui;

import android.app.ActivityOptions;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.AutoTransition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.sbschoolcode.xyzreader.R;
import com.sbschoolcode.xyzreader.adapters.ArticleAdapter;
import com.sbschoolcode.xyzreader.data.ItemsContract;
import com.sbschoolcode.xyzreader.data.UpdaterService;
import com.sbschoolcode.xyzreader.viewmodels.MainViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ArticleClickListener {

    private MainViewModel mMainViewModel;
    private MutableLiveData<Cursor> mAllArticlesLiveData;

    //BroadcastReceiver & IntentFilter
    private RefreshingBroadcastReceiver mRefreshingBroadcastReceiver;
    private IntentFilter mRefreshingIntentFilter;

    @BindView(R.id.main_recycler_view)
    RecyclerView mRecyclerView;
    private ArticleAdapter mArticleAdapter;

    @BindView(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.main_appbar_layout)
    AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new AutoTransition());
            getWindow().setEnterTransition(new AutoTransition());
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(findViewById(R.id.main_toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        mRefreshingBroadcastReceiver = new RefreshingBroadcastReceiver();
        mRefreshingIntentFilter = new IntentFilter();
        mRefreshingIntentFilter.addAction(UpdaterService.BROADCAST_ACTION_STATE_CHANGE);

        mAllArticlesLiveData = new MutableLiveData<>();

        viewSetup();

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mMainViewModel.addObserver(this, cursor -> mAllArticlesLiveData.setValue(cursor));

        mAllArticlesLiveData.observe(this, cursor -> {
            mArticleAdapter.updateData(cursor);
            mArticleAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        });

        if (savedInstanceState == null) startUpdaterService();
    }

    @Override
    protected void onResume() {
        registerReceiver(mRefreshingBroadcastReceiver, mRefreshingIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mRefreshingBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mMainViewModel.refreshData(this);
            return true;
        }
        return false;
    }

    private void viewSetup() {
        //AppBar setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                if (verticalOffset != 0) {
                    if (mAppBarLayout.getElevation() != 4) mAppBarLayout.setElevation(4);
                    mSwipeRefreshLayout.setEnabled(false);
                } else {
                    if (mAppBarLayout.getElevation() != 0) mAppBarLayout.setElevation(0);
                    mSwipeRefreshLayout.setEnabled(true);
                }
            });
        }

        //SwipeRefreshLayout setup
        mSwipeRefreshLayout.setOnRefreshListener(this::startUpdaterService);

        //RecyclerView setup
        mArticleAdapter = new ArticleAdapter(this, null);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2,
                StaggeredGridLayoutManager.VERTICAL));

        mRecyclerView.setAdapter(mArticleAdapter);
    }

    private void startUpdaterService() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    public void onClick(long adapterPosition) {
        Intent detailsActivity = new Intent(this, DetailActivity.class);
        detailsActivity.setData(ItemsContract.Items.buildItemUri(adapterPosition));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(detailsActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else startActivity(detailsActivity);
    }

    class RefreshingBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                boolean isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                mSwipeRefreshLayout.setRefreshing(isRefreshing);

                if (!isRefreshing)
                    mMainViewModel.refreshData(MainActivity.this);
            }
        }
    }
}
