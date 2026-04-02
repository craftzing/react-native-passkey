import {
  handleNativeError,
  NotSupportedError,
  TNativeError,
} from './PasskeyError';
import { Platform } from 'react-native';
import type {
  PasskeyCreateOptions,
  PasskeyCreateRequest,
  PasskeyCreateResult,
  PasskeyGetOptions,
  PasskeyGetRequest,
  PasskeyGetResult,
} from './PasskeyTypes';
import { NativePasskey } from './NativePasskey';

export class Passkey {
  /**
   * Creates a new Passkey
   *
   * @param request The FIDO2 Attestation Request in JSON format
   * @param options An object containing options for the registration process
   * @returns The FIDO2 Attestation Result in JSON format
   * @throws
   */
  public static async create(
    request: PasskeyCreateRequest,
    options?: PasskeyCreateOptions
  ): Promise<PasskeyCreateResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.create(JSON.stringify(request), {
        forcePlatformKey: false,
        forceSecurityKey: false,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
        isConditional: options?.isConditional ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyCreateResult;
      }
      return response as PasskeyCreateResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Creates a new Passkey
   * Forces the usage of a platform authenticator on iOS and Android
   *
   * @param request The FIDO2 Attestation Request in JSON format
   * @param options An object containing options for the registration process
   * @returns The FIDO2 Attestation Result in JSON format
   * @throws
   */
  public static async createPlatformKey(
    request: PasskeyCreateRequest,
    options?: PasskeyCreateOptions
  ): Promise<PasskeyCreateResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.create(JSON.stringify(request), {
        forcePlatformKey: true,
        forceSecurityKey: false,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
        isConditional: options?.isConditional ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyCreateResult;
      }
      return response as PasskeyCreateResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Creates a new Passkey
   * Forces the usage of a security authenticator on iOS and Android
   *
   * @param request The FIDO2 Attestation Request in JSON format
   * @param options An object containing options for the registration process
   * @returns The FIDO2 Attestation Result in JSON format
   * @throws
   */
  public static async createSecurityKey(
    request: PasskeyCreateRequest,
    options?: PasskeyCreateOptions
  ): Promise<PasskeyCreateResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.create(JSON.stringify(request), {
        forcePlatformKey: false,
        forceSecurityKey: true,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
        isConditional: options?.isConditional ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyCreateResult;
      }
      return response as PasskeyCreateResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Authenticates using an existing Passkey
   *
   * @param request The FIDO2 Assertion Request in JSON format
   * @param options An object containing options for the authentication process
   * @returns The FIDO2 Assertion Result in JSON format
   * @throws
   */
  public static async get(
    request: PasskeyGetRequest,
    options?: PasskeyGetOptions
  ): Promise<PasskeyGetResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.get(JSON.stringify(request), {
        forcePlatformKey: false,
        forceSecurityKey: false,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyGetResult;
      }
      return response as PasskeyGetResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Authenticates using an existing Passkey
   * Forces the usage of a platform authenticator on iOS and Android
   *
   * @param request The FIDO2 Assertion Request in JSON format
   * @param options An object containing options for the authentication process
   * @returns The FIDO2 Assertion Result in JSON format
   * @throws
   */
  public static async getPlatformKey(
    request: PasskeyGetRequest,
    options?: PasskeyGetOptions
  ): Promise<PasskeyGetResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.get(JSON.stringify(request), {
        forcePlatformKey: true,
        forceSecurityKey: false,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyGetResult;
      }
      return response as PasskeyGetResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Authenticates using an existing Passkey
   * Forces the usage of a security authenticator on iOS and Android
   *
   * @param request The FIDO2 Assertion Request in JSON format
   * @param options An object containing options for the authentication process
   * @returns The FIDO2 Assertion Result in JSON format
   * @throws
   */
  public static async getSecurityKey(
    request: PasskeyGetRequest,
    options?: PasskeyGetOptions
  ): Promise<PasskeyGetResult> {
    if (!Passkey.isSupported()) {
      throw NotSupportedError;
    }

    try {
      const response = await NativePasskey.get(JSON.stringify(request), {
        forcePlatformKey: false,
        forceSecurityKey: true,
        preferImmediatelyAvailableCredentials:
          options?.preferImmediatelyAvailableCredentials ?? false,
      });

      if (typeof response === 'string') {
        return JSON.parse(response) as PasskeyGetResult;
      }
      return response as PasskeyGetResult;
    } catch (error: unknown) {
      throw handleNativeError(error as TNativeError);
    }
  }

  /**
   * Pre-fetches credential data to reduce latency when subsequently calling get().
   * Should be called early (e.g. on screen load) before the user triggers sign-in.
   * Only has effect on Android 14+ (API level 34); resolves immediately on other platforms.
   *
   * @param request The FIDO2 Assertion Request in JSON format
   */
  public static async prepareGet(request: PasskeyGetRequest): Promise<void> {
    if (Platform.OS !== 'android' || Platform.Version < 34) {
      return;
    }
    await NativePasskey.prepareGet(JSON.stringify(request));
  }

  /**
   * Checks if Passkeys are supported on the current device
   *
   * @returns A boolean indicating whether Passkeys are supported
   */
  public static isSupported(): boolean {
    if (Platform.OS === 'android') {
      return Platform.Version >= 28;
    }

    if (Platform.OS === 'ios') {
      return parseInt(Platform.Version, 10) >= 15;
    }

    return false;
  }
}
