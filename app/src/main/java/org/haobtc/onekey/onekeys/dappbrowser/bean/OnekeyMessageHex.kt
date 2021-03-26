package org.haobtc.onekey.onekeys.dappbrowser.bean

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.haobtc.onekey.utils.HexUtils


data class OnekeyMessageHex(
    val messageHex: String,
    val payload: String,
    val leafPosition: Long) : Signable {
  override fun getMessage(): String {
    try {
      val jsonObject = JsonParser.parseString(payload)
      if (jsonObject.isJsonArray || jsonObject.isJsonArray) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(jsonObject)
      } else {
        return payload.replace("\"", "")
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return payload.replace("\"", "")
    }
  }

  override fun getCallbackId() = leafPosition

  override fun getPrehash(): ByteArray {
    return HexUtils.hexStringToByteArray(messageHex)
  }

  override fun getOrigin() = payload

  override fun getUserMessage() = payload

  override fun getMessageType() = SignMessageType.SIGN_MESSAGE
}
