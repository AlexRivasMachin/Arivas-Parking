package com.lksnext.arivas.data;

public class DataRepository {

    private static DataRepository instance;
    private DataRepository(){

    }

    public static synchronized DataRepository getInstance(){
        if (instance==null){
            instance = new DataRepository();
        }
        return instance;
    }

    public void login(String email, String pass, Callback callback){
        try {
            callback.onSuccess();
        } catch (Exception e){
            callback.onFailure();
        }
    }
}
