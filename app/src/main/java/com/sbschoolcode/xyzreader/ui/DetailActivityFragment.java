package com.sbschoolcode.xyzreader.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbschoolcode.xyzreader.R;
import com.sbschoolcode.xyzreader.data.ArticleLoader;
import com.sbschoolcode.xyzreader.utils.ImageUtils;
import com.sbschoolcode.xyzreader.viewmodels.DetailViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private DetailViewModel mDetailViewModel;

    @BindView(R.id.detail_header_image_view)
    ImageView mHeaderImageView;

    @BindView(R.id.detail_fragment_top_level_layout)
    CoordinatorLayout mTopLevelLayout;

    @BindView(R.id.content_tv)
    TextView mContentText;

    @BindView(R.id.detail_fab)
    FloatingActionButton mFab;

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.header_bar_title)
    TextView mHeaderBarTitle;

    @BindView(R.id.header_bar_subtitle)
    TextView mHeaderBarSubtitle;

    @BindView(R.id.detail_content_read_all_tv)
    TextView mReadAll;

    @BindView(R.id.app_bar_title_tv)
    TextView mAppBarTitle;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //Setup FloatingActionButton
        if (getActivity() != null)
            mFab.setOnClickListener(v -> startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(getString(R.string.fab_action_text))
                    .getIntent(), getString(R.string.fab_action_header))));

        //Setup Toolbar
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        mToolbar.inflateMenu(R.menu.menu_detail);
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                mDetailViewModel.refreshItem(getContext(), mDetailViewModel.getCurrentItem());
                mReadAll.setText(R.string.article_detail_content_expand_message);
                mReadAll.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        //Setup ViewModel
        mDetailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        mDetailViewModel.addObserver(this, this::loadDataFromCursor);
    }

    private void loadDataFromCursor(Cursor cursor) {

        //Check if cursor is null, if it is clean up and return early
        if (cursor == null || cursor.getCount() == 0) {
            mHeaderBarTitle.setText("N/A");
            mHeaderBarSubtitle.setText("N/A");
            mContentText.setText("N/A");
            return;
        }

        //Cursor is not null, continue
        cursor.moveToFirst();

        //Set header and sub-header text
        String title = cursor.getString(ArticleLoader.Query.TITLE);
        mAppBarTitle.setText(title);
        mHeaderBarTitle.setText(title);
        mHeaderBarSubtitle.setText(parseSubHeader(cursor));

        mReadAll.setOnClickListener(v -> {
                mReadAll.setText(R.string.loading);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    mContentText.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)
                            .replaceAll("(\r\n|\n)", "<br />")));
                    mReadAll.setVisibility(View.GONE);
                }, 1000);
        });

        Spanned clippedTextSpanned = Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />"));

        mContentText.setText(TextUtils.concat(clippedTextSpanned.subSequence(0, 500), getString(R.string.ellipses)));

        ImageUtils.seamlessLoadFromUrlToContainer(mHeaderImageView,
                cursor.getString(ArticleLoader.Query.PHOTO_URL),
                mTopLevelLayout);
    }

    public void refreshData(long itemId) {
        mDetailViewModel.refreshItem(getContext(), itemId);
    }

    private Spanned parseSubHeader(Cursor cursor) {
        Date publishedDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US);
        GregorianCalendar startOfEpoch = new GregorianCalendar(2, 1, 1);

        try {
            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            publishedDate = dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(getClass().getSimpleName(), ex.getMessage());
            Log.i(getClass().getSimpleName(), "passing today's date");
            publishedDate = new Date();
        }

        if (!publishedDate.before(startOfEpoch.getTime())) {
            return Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + cursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>");

        } else {
            // If date is before 1902, just show the string
            @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat();

            return Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + cursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>");
        }
    }
}
