package com.sbschoolcode.xyzreader.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbschoolcode.xyzreader.R;
import com.sbschoolcode.xyzreader.data.ArticleLoader;
import com.sbschoolcode.xyzreader.utils.ImageUtils;
import com.sbschoolcode.xyzreader.viewmodels.DetailViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private DetailViewModel mDetailViewModel;
    private long mItemId;

    @BindView(R.id.detail_header_image_view)
    ImageView mHeaderImageView;

    @BindView(R.id.detail_fragment_top_level_layout)
    CoordinatorLayout mTopLevelLayout;

    @BindView(R.id.content_tv)
    TextView mContentTextView;

    @BindView(R.id.detail_fab)
    FloatingActionButton mFab;

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;

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
                mDetailViewModel.refreshItem(getContext(), mItemId);
                return true;
            }
            return false;
        });

        //Setup ViewModel
        mDetailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        mDetailViewModel.addObserver(this, this::loadDataFromCursor);
    }

    private void loadDataFromCursor(Cursor cursor) {

        if (cursor == null || cursor.getCount() == 0) return;

        cursor.moveToFirst();

        //TODO: make show all the text
        Spanned clippedTextSpanned = Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />"));
        mContentTextView.setText(TextUtils.concat(clippedTextSpanned.subSequence(0, 500), getString(R.string.ellipses)));

        ImageUtils.seamlessLoadFromUrlToContainer(mHeaderImageView,
                cursor.getString(ArticleLoader.Query.PHOTO_URL),
                mTopLevelLayout);
    }

    public void refreshData(long itemId) {
        mItemId = itemId;
        mDetailViewModel.refreshItem(getContext(), itemId);
    }
}
