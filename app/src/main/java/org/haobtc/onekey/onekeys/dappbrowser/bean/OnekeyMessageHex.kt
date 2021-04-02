package org.haobtc.onekey.onekeys.dappbrowser.bean

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.orhanobut.logger.Logger
import org.haobtc.onekey.utils.HexUtils
import org.haobtc.onekey.utils.JsonFormatUtil


data class OnekeyMessageHex(
    val messageHex: String,
    val payload: String,
    val leafPosition: Long) : Signable {
  override fun getMessage(): String {
    return try {
      val jsonObject = JsonParser.parseString(payload)
      try {
        JsonFormatUtil.format(jsonObject)
      } catch (e: Exception) {
        if (jsonObject.isJsonArray || jsonObject.isJsonObject) {
          val gson = GsonBuilder().setPrettyPrinting().create()
          gson.toJson(jsonObject)
        } else {
          payload
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      payload.replace("\"", "")
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
