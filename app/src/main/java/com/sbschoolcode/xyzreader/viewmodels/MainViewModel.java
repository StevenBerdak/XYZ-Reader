package com.sbschoolcode.xyzreader.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;

import com.sbschoolcode.xyzreader.repository.DataRepository;

public class MainViewModel extends ViewModel implements MainViewModelInterface {

    private MutableLiveData<Cursor> mAllArticles;
    private DataRepository mDataRepository;

    public MainViewModel() {
        mAllArticles = new MutableLiveData<>();
        mDataRepository = DataRepository.getInstance();
    }

    @Override
    public void refreshData(Context ctx) {
        mAllArticles.postValue(mDataRepository.getAllArticles(ctx));
    }

    @Override
    public void addObserver(LifecycleOwner lifecycleOwner, Observer<Cursor> observer) {
        mAllArticles.observe(lifecycleOwner, observer);
    }

    @Override
    public void removeObserver(Observer<Cursor> observer) {

    }
}
