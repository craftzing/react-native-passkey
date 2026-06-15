package com.reactnativepasskey

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import android.os.Build

import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.*
import androidx.credentials.exceptions.domerrors.*
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.credentials.exceptions.publickeycredential.GetPublicKeyCredentialDomException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PasskeyModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val mainScope = CoroutineScope(Dispatchers.Default)

  override fun getName(): String {
    return "Passkey"
  }

  @ReactMethod
  fun create(requestJson: String, forcePlatformKey: Boolean, forceSecurityKey: Boolean, options: ReadableMap?, promise: Promise) {
    val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
    
    val parsedOptions = parseCustomizationOptions(options)

    val createPublicKeyCredentialRequest = if (Build.VERSION.SDK_INT >= 35) {
      CreatePublicKeyCredentialRequest(
        requestJson = requestJson,
        clientDataHash = null,
        preferImmediatelyAvailableCredentials = parsedOptions.preferImmediatelyAvailable,
        origin = parsedOptions.origin,
        isAutoSelectAllowed = parsedOptions.autoSelectAllowed,
        isConditional = parsedOptions.isConditional
      )
    } else {
      CreatePublicKeyCredentialRequest(requestJson)
    }

    mainScope.launch {
      try {
        val activity = reactApplicationContext.currentActivity
          ?: run { promise.reject("RequestFailed", "No active Activity"); return@launch }

        val result = credentialManager.createCredential(activity, createPublicKeyCredentialRequest)

        val response =
          result.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
            ?: run { promise.reject("UnknownError", "Empty credential response"); return@launch }
        promise.resolve(response)
      } catch (e: CreateCredentialException) {
        val errorCode = handleRegistrationException(e)
        promise.reject(errorCode, errorCode)
      }
    }
  }

  private fun handleRegistrationException(e: CreateCredentialException): String {
    e.printStackTrace()
    when (e) {
      is CreatePublicKeyCredentialDomException -> {
        return mapDomError(e.domError)
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

  @ReactMethod
  fun get(requestJson: String, forcePlatformKey: Boolean, forceSecurityKey: Boolean, preferImmediatelyAvailable: Boolean, options: ReadableMap?, promise: Promise) {
      val credentialManager = CredentialManager.create(reactApplicationContext.applicationContext)
      
      val parsedOptions = parseCustomizationOptions(options)
      val finalPreferImmediatelyAvailable = preferImmediatelyAvailable || parsedOptions.preferImmediatelyAvailable

      val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(requestJson)
      if (parsedOptions.autoSelectAllowed) {
        getPublicKeyCredentialOption.requestData.putBoolean("androidx.credentials.BUNDLE_KEY_IS_AUTO_SELECT_ALLOWED", true)
        getPublicKeyCredentialOption.candidateQueryData.putBoolean("androidx.credentials.BUNDLE_KEY_IS_AUTO_SELECT_ALLOWED", true)
      }

      val getCredentialRequestBuilder = GetCredentialRequest.Builder()
        .addCredentialOption(getPublicKeyCredentialOption)
        .setPreferImmediatelyAvailableCredentials(finalPreferImmediatelyAvailable)

      if (Build.VERSION.SDK_INT >= 35 && parsedOptions.origin != null) {
        getCredentialRequestBuilder.setOrigin(parsedOptions.origin!!)
      }

      val getCredentialRequest = getCredentialRequestBuilder.build()

      mainScope.launch {
        try {
          val activity = reactApplicationContext.currentActivity
            ?: run { promise.reject("RequestFailed", "No active Activity"); return@launch }

          val result = credentialManager.getCredential(activity, getCredentialRequest)

          val response =
            result.credential.data.getString("androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON")
              ?: run { promise.reject("UnknownError", "Empty credential response"); return@launch }
          promise.resolve(response)
        } catch (e: GetCredentialException) {
          val errorCode = handleAuthenticationException(e)
          promise.reject(errorCode, errorCode)
        }
      }
  }

  private fun handleAuthenticationException(e: GetCredentialException): String {
    e.printStackTrace()
    when (e) {
      is GetPublicKeyCredentialDomException -> {
        return mapDomError(e.domError)
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

  private fun mapDomError(domError: DomError): String {
    return when (domError) {
      is InvalidStateError -> "CredentialAlreadyExists"
      is SecurityError -> "RequestFailed"
      is ConstraintError -> "BadConfiguration"
      is NotAllowedError -> "RequestFailed"
      is TimeoutError -> "TimedOut"
      is AbortError -> "UserCancelled"
      is DataError -> "RequestFailed"
      is NotSupportedError -> "NotSupported"
      else -> "UnknownError"
    }
  }

  private fun parseCustomizationOptions(options: ReadableMap?): CustomizationOptions {
    val result = CustomizationOptions()
    if (options == null) return result

    if (options.hasKey("autoSelectAllowed")) {
      result.autoSelectAllowed = options.getBoolean("autoSelectAllowed")
    }
    if (options.hasKey("preferImmediatelyAvailable")) {
      result.preferImmediatelyAvailable = options.getBoolean("preferImmediatelyAvailable")
    }
    if (options.hasKey("origin")) {
      result.origin = options.getString("origin")
    }
    if (options.hasKey("isConditional")) {
      result.isConditional = options.getBoolean("isConditional")
    }
    return result
  }

  class CustomizationOptions {
    var autoSelectAllowed: Boolean = false
    var preferImmediatelyAvailable: Boolean = false
    var origin: String? = null
    var isConditional: Boolean = false
  }
}
