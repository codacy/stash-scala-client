package com.codacy.client.stash.client

final case class RequestResponse[T](
    value: Option[T],
    nextPageStart: Option[Int] = None,
    size: Option[Int] = None,
    limit: Option[Int] = None,
    isLastPage: Option[Boolean] = None,
    message: String = "",
    hasError: Boolean = false
)
