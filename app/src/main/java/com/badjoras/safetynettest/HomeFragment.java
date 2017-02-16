package com.badjoras.safetynettest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badjoras.safetynettest.models.GoogleValdiateBody;
import com.badjoras.safetynettest.retrofit.APIRequest;
import com.badjoras.safetynettest.utils.Constants;
import com.badjoras.safetynettest.utils.JWTUtils;
import com.badjoras.safetynettest.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;

/**
 * Created by baama on 14/02/2017.
 */

public class HomeFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = HomeFragment.class.toString();

    @BindString(R.string.google_safetynet_key) String safetyNetworkKey;
    @BindView(R.id.button_start) TextView buttonStart;
    @BindView(R.id.add_views_container) LinearLayout llContainer;

    private final Random mRandom = new SecureRandom();
    private GoogleApiClient mGoogleApiClient;
    private String safetyNetResult;
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        ButterKnife.bind(this, rootView);

        buildGoogleApiClient();

        return rootView;
    }

    @OnClick(R.id.button_start)
    public void onStartClick(){
        clearScreen();
        sendSafetyNetRequest();
        buttonStart.setEnabled(false);
    }

    /**
     * Generates a 16-byte nonce with additional data.
     * The nonce should also include additional information, such as a user id or any other details
     * you wish to bind to this attestation. Here you can provide a String that is included in the
     * nonce after 24 random bytes. During verification, extract this data again and check it
     * against the request that was made with this nonce.
     */
    private byte[] getRequestNonce(String data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[24];
        mRandom.nextBytes(bytes);
        try {
            byteStream.write(bytes);
            byteStream.write(data.getBytes());
        } catch (IOException e) {
            return null;
        }

        return byteStream.toByteArray();
    }

    /**
     * Constructs an automanaged {@link GoogleApiClient} for the {@link SafetyNet#API}.
     */
    protected synchronized void buildGoogleApiClient() {
        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(SafetyNet.API)
                    .enableAutoManage(getActivity(), this)
                    .build();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Error occurred during connection to Google Play Services that could not be
        // automatically resolved.
        Timber.e("Error connecting to Google Play Services." + connectionResult.getErrorMessage());
    }

    /**
     * https://developer.android.com/training/safetynet/index.html
     */
    private void sendSafetyNetRequest() {
        Timber.i("Sending SafetyNet API request.");
        /*
        Create a nonce for this request.
        The nonce is returned as part of the response from the
        SafetyNet API. Here we append the string to a number of random bytes to ensure it larger
        than the minimum 16 bytes required.
        Read out this value and verify it against the original request to ensure the
        response is correct and genuine.
        NOTE: A nonce must only be used once and a different nonce should be used for each request.
        As a more secure option, you can obtain a nonce from your own server using a secure
        connection. Here in this sample, we generate a String and append random bytes, which is not
        very secure. Follow the tips on the Security Tips page for more information:
        https://developer.android.com/training/articles/security-tips.html#Crypto
         */
        // TODO(developer): Change the nonce generation to include your own, used once value,
        // ideally from your remote server.
        String nonceData = "mezu:" + System.currentTimeMillis();
        byte[] nonce = getRequestNonce(nonceData);

        // Call the SafetyNet API asynchronously. The result is returned through the result callback.
        SafetyNet.SafetyNetApi.attest(mGoogleApiClient, nonce)
                .setResultCallback(new ResultCallback<SafetyNetApi.AttestationResult>() {

                    @Override
                    public void onResult(SafetyNetApi.AttestationResult result) {
                        Status status = result.getStatus();
                        if (status.isSuccess()) {
                            /*
                             Successfully communicated with SafetyNet API.
                             Use result.getJwsResult() to get the signed result data. See the server
                             component of this sample for details on how to verify and parse this
                             result.
                             */
                            safetyNetResult = result.getJwsResult();
                            Timber.i("Success! SafetyNet result:\n" + safetyNetResult + "\n");

                            /*
                             TODO(developer): Forward this result to your server together with
                             the nonce for verification.
                             You can also parse the JwsResult locally to confirm that the API
                             returned a response by checking for an 'error' field first and before
                             retrying the request with an exponential backoff.
                             NOTE: Do NOT rely on a local, client-side only check for security, you
                             must verify the response on a remote server!
                             */
                            addTextViewToScreen(createTextView("Success in Step 1!"));

                            try {
                                //String head = JWTUtils.decodedGetHeader(safetyNetResult);
                                String body = JWTUtils.decodedGetBody(safetyNetResult);

                                //addTextViewToScreen(createTextView(Utils.formatString(head)));
                                addTextViewToScreen(createTextView(Utils.formatString(body)));

                            } catch (Exception e) {
                                e.printStackTrace();
                                Timber.e("ERROR! %s", e.toString());
                            }

                            //addTextViewToScreen(createTextView(" \n SafetyNet result: \n" + safetyNetResult));
                            validateSecondStep(new GoogleValdiateBody(safetyNetResult));

                        } else {
                            addTextViewToScreen(createTextView("ERROR in Step 1!"));
                            // An error occurred while communicating with the service.
                            Timber.e("ERROR! %s %s", status.getStatusCode(), status.getStatusMessage());
                            safetyNetResult = null;

                            addTextViewToScreen(createTextView("code : " + status.getStatusCode()));
                            addTextViewToScreen(createTextView("message : " + status.getStatusMessage()));
                        }
                        buttonStart.setEnabled(true);
                    }
                });
    }

    /**
     * Do NOT rely on a local, client-side only check for security, you
     * must verify the response on a remote server!
     * @param body GoogleValdiateBody object with jwt payload
     */
    private void validateSecondStep(GoogleValdiateBody body){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.GOOGLE_APIS_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();

        APIRequest apiRequest = retrofit.create(APIRequest.class);

        Call<String> validateSSL = apiRequest.validateSslCertificateChain(safetyNetworkKey, body);

        validateSSL.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                addTextViewToScreen(createTextView(" \n Success in Step 2!"));
                addTextViewToScreen(createTextView(Utils.formatString(response.body())));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                addTextViewToScreen(createTextView(" \n ERROR in Step 2!"));
                addTextViewToScreen(createTextView(t.toString()));
            }
        });
    }


    private TextView createTextView(String text){
        TextView valueTV = new TextView(getActivity());
        valueTV.setText(text);
        valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return valueTV;
    }

    private void addTextViewToScreen(TextView tv){
        llContainer.addView(tv);
    }

    private void clearScreen(){
        if(llContainer.getChildCount() > 0)
            llContainer.removeAllViews();
    }
}

