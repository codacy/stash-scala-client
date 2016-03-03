package com.codacy.client.stash.client.auth;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Container for parameters used for calculating OAuth signature.
 * About the only confusing aspect is that of whether entries are to be sorted
 * before encoded or vice versa: if my reading is correct, encoding is to occur
 * first, then sorting; although this should rarely matter (since sorting is primary
 * by key, which usually has nothing to encode)... of course, rarely means that
 * when it would occur it'd be harder to track down.
 */
public class OAuthParameterSet {
    private final ArrayList<Parameter> allParameters;

    public OAuthParameterSet(int size) {
        allParameters = new ArrayList<>(size);
    }

    public OAuthParameterSet add(String key, String value) {
        allParameters.add(new Parameter(key, value));
        return this;
    }

    public String sortAndConcat() {
        // then sort them (AFTER encoding, important)
        Parameter[] params = allParameters.toArray(new Parameter[allParameters.size()]);
        Arrays.sort(params);

        // and build parameter section using pre-encoded pieces:
        StringBuilder encodedParams = new StringBuilder(100);
        for (Parameter param : params) {
            if (encodedParams.length() > 0) {
                encodedParams.append('&');
            }
            encodedParams.append(param.key()).append('=').append(param.value());
        }
        return encodedParams.toString();
    }
}