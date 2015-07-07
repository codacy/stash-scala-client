package com.codacy.client.stash.client

case class RequestResponse[T](value: Option[T], message: String = "", hasError: Boolean = false)
