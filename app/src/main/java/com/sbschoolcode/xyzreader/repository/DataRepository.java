package com.sbschoolcode.xyzreader.repository;

import android.content.Context;
import android.database.Cursor;

import com.sbschoolcode.xyzreader.data.ArticleLoader;

public class DataRepository implements DataRepositoryInterface {

    private static DataRepository mInstance;

    private DataRepository() {

    }

    public static DataRepository getInstance() {
        if (null == mInstance)
            mInstance = new DataRepository();

        return mInstance;
    }

    @Override
    public Cursor getAllArticles(Context ctx) {
        return ArticleLoader.newAllArticlesInstance(ctx).loadInBackground();
    }

    @Override
    public Cursor getSpecificArticleData(Context ctx, long itemId) {
        return ArticleLoader.newInstanceForItemId(ctx, itemId).loadInBackground();
    }
}
