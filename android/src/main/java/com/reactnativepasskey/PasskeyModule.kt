package com.reactnativepasskey

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PrepareGetCredentialResponse
import androidx.credentials.exceptions.*
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.credentials.exceptions.publickeycredential.GetPublicKeyCredentialDomException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import android.os.Build
import org.json.JSONObject

class PasskeyModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val mainScope = CoroutineScope(Dispatchers.Default)
  private var preparedGetResponse: PrepareGetCredentialResponse? = null

  override fun getName(): String {
    return "Passkey"
  }

  @ReactMethod
  fun create(requestJson: String, options: ReadableMap, promise: Promise) {
    val preferImmediatelyAvailable = options.getBoolean("preferImmediatelyAvailableCredentials")
    val isConditional = options.getBoolean("isConditional")
    val forcePlatformKey = options.getBoolean("forcePlatformKey")
    val forceSecurityKey = options.getBoolean("forceSecurityKey")

    val adjustedJson = adjustAuthenticatorAttachment(requestJson, forcePlatformKey, forceSecurityKey)

    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
      requestJson = adjustedJson,
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailable,
      isConditional = isConditional
    )

    mainScope.launch {
      try {
        val result = reactApplicationContext.currentActivity?.let { credentialManager.createCredential(it, createPublicKeyCredentialRequest) }

        val response =
          result?.data?.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
        promise.resolve(response)
      } catch (e: CreateCredentialException) {
        val errorCode = handleRegistrationException(e)
        promise.reject(errorCode, errorCode)
      }
    }
  }

  @ReactMethod
  fun get(requestJson: String, options: ReadableMap, promise: Promise) {
    val preferImmediatelyAvailable = options.getBoolean("preferImmediatelyAvailableCredentials")

    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    val getCredentialRequest = GetCredentialRequest(
      credentialOptions = listOf(GetPublicKeyCredentialOption(requestJson)),
      preferImmediatelyAvailableCredentials = preferImmediatelyAvailable
    )

    mainScope.launch {
      try {
        val storedPreparedResponse = preparedGetResponse
        val result = if (storedPreparedResponse != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          preparedGetResponse = null
          reactApplicationContext.currentActivity?.let {
            credentialManager.getCredential(it, getCredentialRequest, storedPreparedResponse)
          }
        } else {
          reactApplicationContext.currentActivity?.let {
            credentialManager.getCredential(it, getCredentialRequest)
          }
        }

        val response =
          result?.credential?.data?.getString("androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON")
        promise.resolve(response)
      } catch (e: GetCredentialException) {
        val errorCode = handleAuthenticationException(e)
        promise.reject(errorCode, errorCode)
      }
    }
  }

  @ReactMethod
  fun prepareGet(requestJson: String, promise: Promise) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      promise.resolve(null)
      return
    }

    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    val getCredentialRequest = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson)))

    mainScope.launch {
      try {
        preparedGetResponse = credentialManager.prepareGetCredential(getCredentialRequest)
        promise.resolve(null)
      } catch (e: GetCredentialException) {
        val errorCode = handleAuthenticationException(e)
        promise.reject(errorCode, errorCode)
      }
    }
  }

  private fun adjustAuthenticatorAttachment(
    requestJson: String,
    forcePlatformKey: Boolean,
    forceSecurityKey: Boolean
  ): String {
    if (!forcePlatformKey && !forceSecurityKey) return requestJson
    val json = JSONObject(requestJson)
    val authSelection = json.optJSONObject("authenticatorSelection") ?: JSONObject()
    authSelection.put("authenticatorAttachment", if (forcePlatformKey) "platform" else "cross-platform")
    json.put("authenticatorSelection", authSelection)
    return json.toString()
  }

  private fun handleRegistrationException(e: CreateCredentialException): String {
    e.printStackTrace()
    when (e) {
      is CreatePublicKeyCredentialDomException -> {
        return e.errorMessage.toString()
      }
      is CreateCredentialCancellationException -> {
        return "UserCancelled"
      }
      is CreateCredentialInterruptedException -> {
        return "Interrupted"
      }
      is CreateCredentialProviderConfigurationException -> {
        return "NotConfigured"
      }
      is CreateCredentialUnknownException -> {
        return "UnknownError"
      }
      is CreateCredentialUnsupportedException -> {
        return "NotSupported"
      }
      else -> {
        return e.errorMessage.toString()
      }
    }
  }

  private fun handleAuthenticationException(e: GetCredentialException): String {
    e.printStackTrace()
    when (e) {
      is GetPublicKeyCredentialDomException -> {
        return e.errorMessage.toString()
      }
      is GetCredentialCancellationException -> {
        return "UserCancelled"
      }
      is GetCredentialInterruptedException -> {
        return "Interrupted"
      }
      is GetCredentialProviderConfigurationException -> {
        return "NotConfigured"
      }
      is GetCredentialUnknownException -> {
        return "UnknownError"
      }
      is GetCredentialUnsupportedException -> {
        return "NotSupported"
      }
      is NoCredentialException -> {
        return "NoCredentials"
      }
      else -> {
        return e.errorMessage.toString()
      }
    }
  }
}
