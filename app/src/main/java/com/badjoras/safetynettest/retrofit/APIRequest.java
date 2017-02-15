package com.badjoras.safetynettest.retrofit;

import com.badjoras.safetynettest.models.GoogleValdiateBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by baama on 14/02/2017.
 */

public interface APIRequest {

    @POST(EndPoints.GOOGLE_ANDROID_VERIFY)
    Call<String> validateSslCertificateChain(@Query("key") String key, @Body GoogleValdiateBody body);

}
