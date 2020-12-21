package com.example.amvchat.ModelClasses;

import com.example.amvchat.Notifications.MyResponse;
import com.example.amvchat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAylBU9VM:APA91bGp44_CCZ2aMSHHMmBoAIAKkIPngvc-P4Yv9ajQYG-MOU0Q9Fw4ZhG9sdcjKJkeiOclKiWiviywtwHsD87rBDm7FQAjVGwrCfIVUwS8KtmBFFlUY-niqHhKhhAF1OUzlsG2PRuV"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
