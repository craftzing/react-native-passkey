/**
 * The FIDO2 Attestation Request
 * https://www.w3.org/TR/webauthn-3/#dictionary-makecredentialoptions
 */
export type PasskeyBinaryValue = Uint8Array | ArrayBuffer | number[] | string;

export interface PasskeyCreateRequest {
  challenge: string;
  rp: {
    id: string;
    name: string;
  };
  user: {
    id: string;
    name: string;
    displayName: string;
  };
  pubKeyCredParams: Array<{ type: 'public-key'; alg: number }>;
  timeout?: number;
  excludeCredentials?: Array<PublicKeyCredentialDescriptor>;
  authenticatorSelection?: {
    authenticatorAttachment?: 'platform' | 'cross-platform';
    requireResidentKey?: boolean;
    residentKey?: 'discouraged' | 'preferred' | 'required';
    userVerification?: 'discouraged' | 'preferred' | 'required';
  };
  attestation?: 'none' | 'indirect' | 'direct' | 'enterprise';
  extensions?: {
    largeBlob?: {
      support?: 'preferred' | 'required';
      read?: boolean;
      write?: PasskeyBinaryValue;
    };
    prf?: {
      eval?: AuthenticationExtensionsPRFValues;
      evalByCredential?:
        | Record<string, AuthenticationExtensionsPRFValues>
        | Array<Record<string, AuthenticationExtensionsPRFValues>>;
    };
  };
}

/**
 * The FIDO2 Attestation Result
 * https://www.w3.org/TR/webauthn-3/#iface-pkcredential
 */
export interface PasskeyCreateResult {
  id: string;
  rawId: string;
  type?: string;
  authenticatorAttachment?: string;
  response: {
    clientDataJSON: string;
    attestationObject: string;
    authenticatorData?: string;
    transports?: Array<AuthenticatorTransport>;
    publicKeyAlgorithm?: number;
    publicKey?: string;
  };
  clientExtensionResults?: {
    largeBlob?: {
      supported?: boolean;
      blob?: Record<string, number>;
      written?: boolean;
    };
    prf?: {
      enabled?: boolean;
      results?: AuthenticationExtensionsPRFValues;
    };
  };
}

/**
 * The FIDO2 Assertion Request
 * https://www.w3.org/TR/webauthn-3/#dictionary-assertion-options
 */
export interface PasskeyGetRequest {
  challenge: string;
  rpId: string;
  timeout?: number;
  allowCredentials?: Array<PublicKeyCredentialDescriptor>;
  userVerification?: 'discouraged' | 'preferred' | 'required';
  extensions?: {
    largeBlob?: {
      read?: boolean;
      write?: PasskeyBinaryValue;
    };
    prf?: {
      eval?: AuthenticationExtensionsPRFValues;
      evalByCredential?:
        | Record<string, AuthenticationExtensionsPRFValues>
        | Array<Record<string, AuthenticationExtensionsPRFValues>>;
    };
  };
}

/**
 * The FIDO2 Assertion Result
 * https://www.w3.org/TR/webauthn-3/#iface-pkcredential
 */
export interface PasskeyGetResult {
  id: string;
  rawId?: string;
  type?: string;
  authenticatorAttachment?: string;
  response: {
    authenticatorData: string;
    clientDataJSON: string;
    signature: string;
    userHandle?: string;
    attestationObject?: string;
  };
  clientExtensionResults?: {
    largeBlob?: {
      supported?: boolean;
      blob?: Record<string, number>;
      written?: boolean;
    };
    prf?: {
      enabled?: boolean;
      results?: AuthenticationExtensionsPRFValues;
    };
  };
}

// https://www.w3.org/TR/webauthn-3/#dictionary-credential-descriptor
export interface PublicKeyCredentialDescriptor {
  type: 'public-key';
  id: string;
  transports?: Array<AuthenticatorTransport>;
}

export enum AuthenticatorTransport {
  usb = 'usb',
  nfc = 'nfc',
  ble = 'ble',
  smartCard = 'smart-card',
  hybrid = 'hybrid',
  internal = 'internal',
}

/**
 * https://www.w3.org/TR/webauthn-3/#prf-extension
 */
export interface AuthenticationExtensionsPRFValues {
  first: PasskeyBinaryValue;
  second?: PasskeyBinaryValue;
}

export interface BaseAndroid15CustomizationOptions {
  /**
   * If true, allows the system to automatically select a credential without
   * prompting the user with a dialog, provided there is exactly one matching credential.
   */
  autoSelectAllowed?: boolean;
  /**
   * If true, specifies a preference for credentials that are immediately available on the device
   * (e.g. local biometrics) rather than initiating a flow that requires external devices
   * (like security keys or another phone via QR code).
   *
   * If no local credentials are immediately available, the operation will fail silently
   * with a 'NoCredentials' error.
   */
  preferImmediatelyAvailable?: boolean;
  themeVariant?: 'system' | 'light' | 'dark';
  displayHint?: {
    title?: string;
    subtitle?: string;
  };
}

export interface Android15CreateCustomizationOptions
  extends BaseAndroid15CustomizationOptions {
  /**
   * If true, enables silent passkey creation (conditional registration). The system
   * attempts to create the passkey in the background without immediately showing a popup dialog.
   *
   * NOTE: This requires that the user already has a saved password credential for the same
   * account in their password manager (e.g., Google Password Manager). If this condition is not met,
   * the call will fail with a 'NoCreateOption' error, and the app should fall back to calling
   * `Passkey.create` with `isConditional: false` (an interactive prompt).
   */
  isConditional?: boolean;
}

export interface PasskeyCreateOptions {
  androidOptions?: Android15CreateCustomizationOptions;
}

export interface Android15GetCustomizationOptions
  extends BaseAndroid15CustomizationOptions {}

export interface PasskeyGetOptions {
  androidOptions?: Android15GetCustomizationOptions;
}
