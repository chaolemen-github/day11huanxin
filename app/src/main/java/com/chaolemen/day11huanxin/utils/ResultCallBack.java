package com.chaolemen.day11huanxin.utils;

public interface ResultCallBack {
    void onSuccess(String filePath, long duration);
    void onFail(String str);
}
