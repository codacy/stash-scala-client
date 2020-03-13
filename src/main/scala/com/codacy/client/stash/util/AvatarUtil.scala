package com.codacy.client.stash.util

object AvatarUtil {

  /**
    * Size in pixels to return avatars
    */
  val defaultAvatarSize = 64

  /**
    * Bitbucket Server accepts a parameter on most endpoints to include avatars in the response. Calling this method
    * with true will add the parameter required, if false an empty map is returned.
    */
  def addAvatarToParams(includeAvatar: Boolean): Map[String, String] = {
    if (includeAvatar) Map("avatarSize" -> defaultAvatarSize.toString) else Map.empty[String, String]
  }

}
