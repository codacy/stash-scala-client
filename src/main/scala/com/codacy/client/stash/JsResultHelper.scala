package com.codacy.client.stash

import play.api.libs.json.JsonValidationError

object JsResultHelper {

  def error(error: String): JsonValidationError = JsonValidationError(error)

}
