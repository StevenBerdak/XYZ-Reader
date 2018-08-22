package com.sbschoolcode.xyzreader.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.database.Cursor;

interface DetailViewModelInterface {
    void refreshItem(Context ctx, long itemId);

    void addObserver(LifecycleOwner lifecycleOwner, Observer<Cursor> observer);

    void removeObserver(Observer<Cursor> observer);
}
