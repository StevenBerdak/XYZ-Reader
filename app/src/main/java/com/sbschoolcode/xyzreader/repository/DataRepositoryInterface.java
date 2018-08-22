package com.sbschoolcode.xyzreader.repository;

import android.content.Context;
import android.database.Cursor;

interface DataRepositoryInterface {

    Cursor getAllArticles(Context ctx);

    Cursor getSpecificArticleData(Context ctx, long itemId);
}
