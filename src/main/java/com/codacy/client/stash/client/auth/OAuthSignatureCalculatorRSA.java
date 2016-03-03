package com.codacy.client.stash.client.auth;

/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

import com.ning.http.client.Param;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilderBase;
import com.ning.http.client.SignatureCalculator;
import com.ning.http.client.oauth.ConsumerKey;
import com.ning.http.client.oauth.RequestToken;
import com.ning.http.client.uri.Uri;
import com.ning.http.util.Base64;
import com.ning.http.util.StringUtils;
import com.ning.http.util.UTF8UrlEncoder;
import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthMessageSignerException;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.ning.http.util.MiscUtils.isNonEmpty;

/**
 * Simple OAuth signature calculator that can used for constructing client signatures
 * for accessing services that use OAuth for authorization.
 * <p>
 * Supports most common signature inclusion and calculation methods: HMAC-SHA1 for
 * calculation, and Header inclusion as inclusion method. Nonce generation uses
 * simple random numbers with base64 encoding.
 *
 * @author tatu (tatu.saloranta@iki.fi)
 */
public class OAuthSignatureCalculatorRSA implements SignatureCalculator {
    public final static String HEADER_AUTHORIZATION = "Authorization";

    private static final String KEY_OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String KEY_OAUTH_NONCE = "oauth_nonce";
    private static final String KEY_OAUTH_SIGNATURE = "oauth_signature";
    private static final String KEY_OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String KEY_OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String KEY_OAUTH_TOKEN = "oauth_token";
    private static final String KEY_OAUTH_VERSION = "oauth_version";

    private static final String OAUTH_VERSION_1_0 = "1.0";
    private static final String OAUTH_SIGNATURE_METHOD = "RSA-SHA1";

    protected static final ThreadLocal<byte[]> NONCE_BUFFER = new ThreadLocal<byte[]>() {
        protected byte[] initialValue() {
            return new byte[16];
        }
    };

    protected final ConsumerKey consumerAuth;

    protected final RequestToken userAuth;

    /**
     * @param consumerAuth Consumer key to use for signature calculation
     * @param userAuth     Request/access token to use for signature calculation
     */
    public OAuthSignatureCalculatorRSA(ConsumerKey consumerAuth, RequestToken userAuth) {
        this.consumerAuth = consumerAuth;
        this.userAuth = userAuth;
    }

    @Override
    public void calculateAndAddSignature(Request request, RequestBuilderBase<?> requestBuilder) {
        String nonce = generateNonce();
        long timestamp = generateTimestamp();
        String signature = null;
        try {
            signature = calculateSignature(request.getMethod(), request.getUri(), timestamp, nonce, request.getFormParams(), request.getQueryParams());
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        }
        String headerValue = constructAuthHeader(signature, nonce, timestamp);
        requestBuilder.setHeader(HEADER_AUTHORIZATION, headerValue);
    }

    private String baseUrl(Uri uri) {
        /* 07-Oct-2010, tatu: URL may contain default port number; if so, need to extract
         *   from base URL.
         */
        String scheme = uri.getScheme();

        StringBuilder sb = StringUtils.stringBuilder();
        sb.append(scheme).append("://").append(uri.getHost());

        int port = uri.getPort();
        if (scheme.equals("http")) {
            if (port == 80)
                port = -1;
        } else if (scheme.equals("https")) {
            if (port == 443)
                port = -1;
        }

        if (port != -1)
            sb.append(':').append(port);

        if (isNonEmpty(uri.getPath()))
            sb.append(uri.getPath());

        return sb.toString();
    }

    private String encodedParams(long oauthTimestamp, String nonce, List<Param> formParams, List<Param> queryParams) {
        /**
         * List of all query and form parameters added to this request; needed
         * for calculating request signature
         */
        int allParametersSize = 5
                + (userAuth.getKey() != null ? 1 : 0)
                + (formParams != null ? formParams.size() : 0)
                + (queryParams != null ? queryParams.size() : 0);
        OAuthParameterSet allParameters = new OAuthParameterSet(allParametersSize);

        // start with standard OAuth parameters we need
        allParameters.add(KEY_OAUTH_CONSUMER_KEY, consumerAuth.getKey());
        allParameters.add(KEY_OAUTH_NONCE, nonce);
        allParameters.add(KEY_OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD);
        allParameters.add(KEY_OAUTH_TIMESTAMP, String.valueOf(oauthTimestamp));
        if (userAuth.getKey() != null) {
            allParameters.add(KEY_OAUTH_TOKEN, userAuth.getKey());
        }
        allParameters.add(KEY_OAUTH_VERSION, OAUTH_VERSION_1_0);

        if (formParams != null) {
            for (Param param : formParams) {
                allParameters.add(param.getName(), param.getValue());
            }
        }
        if (queryParams != null) {
            for (Param param : queryParams) {
                allParameters.add(param.getName(), param.getValue());
            }
        }
        return allParameters.sortAndConcat();
    }

    /**
     * Method for calculating OAuth signature using HMAC/SHA-1 method.
     */
    public String calculateSignature(String method, Uri uri, long oauthTimestamp, String nonce,
                                     List<Param> formParams, List<Param> queryParams) throws OAuthMessageSignerException {

        // beware: must generate first as we're using pooled StringBuilder
        String baseUrl = baseUrl(uri);
        String encodedParams = encodedParams(oauthTimestamp, nonce, formParams, queryParams);

        StringBuilder sb = StringUtils.stringBuilder();
        sb.append(method); // POST / GET etc (nothing to URL encode)
        sb.append('&');
        UTF8UrlEncoder.encodeAndAppendQueryElement(sb, baseUrl);


        // and all that needs to be URL encoded (... again!)
        sb.append('&');
        UTF8UrlEncoder.encodeAndAppendQueryElement(sb, encodedParams);

        return sign(sb);
    }


    private String sign(StringBuilder sb) throws OAuthMessageSignerException {
        try {
            byte[] keyBytes = sb.toString().getBytes(OAuth.ENCODING);
            byte[] signedBytes = signBytes(keyBytes);
            return base64Encode(signedBytes).trim();
        } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            throw new OAuthMessageSignerException(new Exception(e));
        }
    }

    private String base64Encode(byte[] b) {
        org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
        return new String(base64.encode(b));
    }

    private byte[] signBytes(byte[] keyBytes) throws SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(getPrivateKey());
        signer.update(keyBytes);
        return signer.sign();
    }

    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyString = consumerAuth.getSecret();
        byte[] privateKeyBytes = Base64.decode(privateKeyString);
        KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        return privateKeyFactory.generatePrivate(privateKeySpec);
    }


    /**
     * Method used for constructing
     */
    private String constructAuthHeader(String signature, String nonce, long oauthTimestamp) {
        StringBuilder sb = StringUtils.stringBuilder();
        sb.append("OAuth ");
        sb.append(KEY_OAUTH_CONSUMER_KEY).append("=\"").append(consumerAuth.getKey()).append("\", ");
        if (userAuth.getKey() != null) {
            sb.append(KEY_OAUTH_TOKEN).append("=\"").append(userAuth.getKey()).append("\", ");
        }
        sb.append(KEY_OAUTH_SIGNATURE_METHOD).append("=\"").append(OAUTH_SIGNATURE_METHOD).append("\", ");

        // careful: base64 has chars that need URL encoding:
        sb.append(KEY_OAUTH_SIGNATURE).append("=\"");
        UTF8UrlEncoder.encodeAndAppendQueryElement(sb, signature).append("\", ");
        sb.append(KEY_OAUTH_TIMESTAMP).append("=\"").append(oauthTimestamp).append("\", ");

        // also: nonce may contain things that need URL encoding (esp. when using base64):
        sb.append(KEY_OAUTH_NONCE).append("=\"");
        UTF8UrlEncoder.encodeAndAppendQueryElement(sb, nonce);
        sb.append("\", ");

        sb.append(KEY_OAUTH_VERSION).append("=\"").append(OAUTH_VERSION_1_0).append("\"");
        return sb.toString();
    }

    protected long generateTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    protected String generateNonce() {
        byte[] nonceBuffer = NONCE_BUFFER.get();
        ThreadLocalRandom.current().nextBytes(nonceBuffer);
        // let's use base64 encoding over hex, slightly more compact than hex or decimals
        return Base64.encode(nonceBuffer);
//      return String.valueOf(Math.abs(random.nextLong()));
    }

    /**
     * Container for parameters used for calculating OAuth signature.
     * About the only confusing aspect is that of whether entries are to be sorted
     * before encoded or vice versa: if my reading is correct, encoding is to occur
     * first, then sorting; although this should rarely matter (since sorting is primary
     * by key, which usually has nothing to encode)... of course, rarely means that
     * when it would occur it'd be harder to track down.
     */
    final static class OAuthParameterSet {
        private final ArrayList<Parameter> allParameters;

        public OAuthParameterSet(int size) {
            allParameters = new ArrayList<>(size);
        }

        public OAuthParameterSet add(String key, String value) {
            Parameter p = new Parameter(UTF8UrlEncoder.encode(key), UTF8UrlEncoder.encode(value));
            allParameters.add(p);
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

    /**
     * Helper class for sorting query and form parameters that we need
     */
    final static class Parameter implements Comparable<Parameter> {
        private final String key, value;

        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }

        @Override
        public int compareTo(Parameter other) {
            int diff = key.compareTo(other.key);
            if (diff == 0) {
                diff = value.compareTo(other.value);
            }
            return diff;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Parameter parameter = (Parameter) o;

            if (!key.equals(parameter.key)) return false;
            if (!value.equals(parameter.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }
}