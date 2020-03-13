package com.codacy.client.stash.util

object AvatarUtils {

  /**
    * Size in pixels to return avatars
    */
  val defaultAvatarSize = 64

  def avatarParams: Map[String, String] = {
    Map("avatarSize" -> defaultAvatarSize.toString)
  }

}
