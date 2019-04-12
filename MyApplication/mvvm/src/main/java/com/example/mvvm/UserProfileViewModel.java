package com.example.mvvm;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class UserProfileViewModel extends ViewModel {
    private String userId;
    private MutableLiveData<User> user;

    public void init(String userId){
        this.userId = userId;
        user = new MutableLiveData<>();
    }

    public void refresh(String userId){
        user = new MutableLiveData<>();
    }

    public MutableLiveData<User> getUser(){
        return user;
    }
}
