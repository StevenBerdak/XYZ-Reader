package com.sbschoolcode.xyzreader.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;

import com.sbschoolcode.xyzreader.repository.DataRepository;

public class DetailViewModel extends ViewModel implements DetailViewModelInterface {

    private MutableLiveData<Cursor> mItemLiveData;
    private DataRepository mDataRepository;
    private long mCurrentItem;

    public DetailViewModel() {
        mItemLiveData = new MutableLiveData<>();
        mDataRepository = DataRepository.getInstance();
    }


    @Override
    public void clearItem() {

    }

    @Override
    public void refreshItem(Context ctx, long itemId) {
        mCurrentItem = itemId;
        mItemLiveData.postValue(mDataRepository.getSpecificArticleData(ctx, itemId));
    }

    @Override
    public void addObserver(LifecycleOwner lifecycleOwner, Observer<Cursor> observer) {
        mItemLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void removeObserver(Observer<Cursor> observer) {
        mItemLiveData.removeObserver(observer);
    }

    public long getCurrentItem() {
        return mCurrentItem;
    }
}
