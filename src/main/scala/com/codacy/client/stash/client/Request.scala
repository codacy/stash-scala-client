package com.codacy.client.stash.client

case class Request[T](url: String, classType: Class[T])
