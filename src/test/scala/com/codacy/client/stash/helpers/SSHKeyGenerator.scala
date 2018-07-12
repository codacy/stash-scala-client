package com.codacy.client.stash.helpers

import java.io.ByteArrayOutputStream
import com.jcraft.jsch._

object SSHKeyGenerator {

  def generateKey(keyType: Int = KeyPair.RSA, size: Int = 4096): (String, String) = {

      val keyPair = KeyPair.genKeyPair(new JSch(), keyType, size)

      val publicStream = new ByteArrayOutputStream
      val privateStream = new ByteArrayOutputStream

      keyPair.writePublicKey(publicStream, "Codacy account public key")
      keyPair.writePrivateKey(privateStream, null)

      (publicStream.toString("UTF-8"), privateStream.toString("UTF-8"))

  }

}
