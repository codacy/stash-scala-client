package com.codacy.client.stash.client

final case class Request[T](path: String, classType: Class[T])
