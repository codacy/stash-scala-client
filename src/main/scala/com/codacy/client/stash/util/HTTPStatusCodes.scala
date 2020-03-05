package com.codacy.client.stash.util

object HTTPStatusCodes {
  val OK = 200
  val CREATED = 201
  val NO_CONTENT = 204
  val BAD_REQUEST = 400
  val UNAUTHORIZED = 401
  val FORBIDDEN = 403
  val NOT_FOUND = 404

  //Redirects
  object Redirects {
    val MOVED_PERMANENTLY = 301
    val FOUND = 302
    val SEE_OTHER = 303
    val TEMPORARY_REDIRECT = 307
    val PERMANENT_REDIRECT = 308

    val all = Seq(MOVED_PERMANENTLY, FOUND, SEE_OTHER, TEMPORARY_REDIRECT, PERMANENT_REDIRECT)
  }
}
