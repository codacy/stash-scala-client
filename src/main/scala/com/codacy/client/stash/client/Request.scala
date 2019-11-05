package com.codacy.client.stash.client

final case class Request[T](url: String, classType: Class[T])
