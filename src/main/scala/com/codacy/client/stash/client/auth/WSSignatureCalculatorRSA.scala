package com.codacy.client.stash.client.auth

import com.ning.http.client.oauth.{ConsumerKey, RequestToken}
import play.api.libs.ws.WSSignatureCalculator

class WSSignatureCalculatorRSA(consumerAuth: ConsumerKey, userAuth: RequestToken) extends OAuthSignatureCalculatorRSA(consumerAuth, userAuth) with WSSignatureCalculator
