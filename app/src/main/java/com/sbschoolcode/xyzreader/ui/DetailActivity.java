package com.sbschoolcode.xyzreader.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.WindowManager;

import com.sbschoolcode.xyzreader.R;
import com.sbschoolcode.xyzreader.data.ItemsContract;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);


        //Clear system bar hack
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                long mItemId = ItemsContract.Items.getItemId(getIntent().getData());
                updateFragmentData(mItemId);
            }

            int navigationBarHeight = navBarHeight(this);
            if (navigationBarHeight > 0)
                adjustFragmentFabHeight(navigationBarHeight);
        }
    }

    private void adjustFragmentFabHeight(int navigationBarHeight) {
        DetailActivityFragment detailFragment = (DetailActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
        detailFragment.adjustFabHeight(navigationBarHeight);
    }

    private void updateFragmentData(long itemId) {
        DetailActivityFragment detailFragment = (DetailActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
        detailFragment.refreshData(itemId);
    }

    public int navBarHeight(Context context) {
        Point actualSize = new Point();
        Point usableSize = new Point();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (windowManager != null)
            display = windowManager.getDefaultDisplay();

        if (display != null) {
            display.getSize(usableSize);
            display.getRealSize(actualSize);
        }

        if (usableSize.y < actualSize.y)
            return actualSize.y - usableSize.y;

        return 0;
    }
}
