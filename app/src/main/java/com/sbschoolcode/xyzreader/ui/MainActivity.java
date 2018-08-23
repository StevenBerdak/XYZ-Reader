package com.sbschoolcode.xyzreader.ui;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.sbschoolcode.xyzreader.AppUtils;
import com.sbschoolcode.xyzreader.R;
import com.sbschoolcode.xyzreader.adapters.ArticleAdapter;
import com.sbschoolcode.xyzreader.data.ItemsContract;
import com.sbschoolcode.xyzreader.data.UpdaterService;
import com.sbschoolcode.xyzreader.viewmodels.MainViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ArticleClickListener {

    private static final String OUT_STATE_ITEM_POSITION = "out_state_item_position";
    private MainViewModel mMainViewModel;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private ArticleAdapter mArticleAdapter;
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;
    private int mScrollTo;

    //BroadcastReceiver & IntentFilter
    private RefreshingBroadcastReceiver mRefreshingBroadcastReceiver;
    private IntentFilter mRefreshingIntentFilter;

    @BindView(R.id.main_recycler_view)
    RecyclerView mRecyclerView;

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

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewSetup();

        mMainViewModel.addObserver(this, cursor -> {
            if (cursor == null || cursor.getCount() == 0) {
                formErrorMessage();
                return;
            }

            mArticleAdapter.updateData(cursor);

            mSwipeRefreshLayout.setRefreshing(false);

            if (mScrollTo != 0)
                new Handler(getMainLooper()).postDelayed(() -> mRecyclerView.scrollToPosition(mScrollTo), 1000);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int itemPosition = mStaggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null)[0];
        itemPosition = clamp(itemPosition, 0, mRecyclerView.getChildCount() - 1);
        outState.putInt(OUT_STATE_ITEM_POSITION, itemPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mStaggeredGridLayoutManager.onRestoreInstanceState(null);
        mScrollTo = savedInstanceState.getInt(OUT_STATE_ITEM_POSITION);

        Log.v(getClass().getSimpleName(), "test : " + mArticleAdapter.getItemCount());
    }

    @Override
    protected void onResume() {
        registerReceiver(mRefreshingBroadcastReceiver, mRefreshingIntentFilter);
        super.onResume();
        if (mArticleAdapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(true);
            startUpdaterService();
        }
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
            startUpdaterService();
            return true;
        }
        return false;
    }

    private int clamp(int x, int lowerBound, int upperBound) {
        if (x < lowerBound) return lowerBound;
        if (x > upperBound) return upperBound;
        return x;
    }

    private void formErrorMessage() {
        StringBuilder stringBuilder = new StringBuilder(getString(R.string.uh_oh_base));
        if (!AppUtils.isNetworkAvailable(this)) {
            stringBuilder.append(" ").append(getString(R.string.uh_oh_internet));
        }
        AppUtils.summonSnackbarSelfClosing(mSwipeRefreshLayout, stringBuilder.toString());
    }

    private void viewSetup() {
        //AppBar setup
        mOnRefreshListener = this::startUpdaterService;
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (verticalOffset != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mAppBarLayout.getElevation() != 4) mAppBarLayout.setElevation(4);
                }
                mSwipeRefreshLayout.setOnRefreshListener(null);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mAppBarLayout.getElevation() != 0) mAppBarLayout.setElevation(0);
                }
                mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
            }
        });


        //RecyclerView setup
        mArticleAdapter = new ArticleAdapter(this, null);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(
                getOrientation() == Configuration.ORIENTATION_PORTRAIT ? 1 : 2,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        mRecyclerView.setAdapter(mArticleAdapter);
    }

    private void startUpdaterService() {
        mScrollTo = 0;
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

    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    class RefreshingBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {

                boolean isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                mSwipeRefreshLayout.setRefreshing(isRefreshing);

                if (!isRefreshing)
                    mMainViewModel.updateData(MainActivity.this);
            }
        }
    }
}
