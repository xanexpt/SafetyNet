package com.badjoras.safetynettest.models;

/**
 * Created by baama on 14/02/2017.
 */

public class GoogleValdiateBody {

    public String signedAttestation;

    public GoogleValdiateBody(String signedAttestation) {
        this.signedAttestation = signedAttestation;
    }

    public String getSignedAttestation() {
        return signedAttestation;
    }

    public void setSignedAttestation(String signedAttestation) {
        this.signedAttestation = signedAttestation;
    }
}
