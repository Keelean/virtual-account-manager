package com.keelean.legacy.customeraccounts.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

@Slf4j
public class EncryptionUtils {

   /** The Constant ALGORITHM. */
	 private static final String ALGORITHM = "AES";

  public static void encryptNode(JsonNode parent, String fieldName, String secretKey)
      throws Exception {
    if (parent.has(fieldName)) {
      String value = ((ObjectNode) parent).get(fieldName).toString();
      if (!isEncrypted(value, secretKey)) {
        ((ObjectNode) parent).put(fieldName, encrypt(value, secretKey));
      }
    }
    for (JsonNode child : parent) {
      encryptNode(child, fieldName, secretKey);
    }
  }

  /**
   * Checks if is encrypted.
   *
   * @param data the data
   * @param secretKey the secret key
   * @return true, if is encrypted
   * @throws Exception the exception
   */
  public static boolean isEncrypted(String data, String secretKey) throws Exception {
    try {
      decrypt(data, secretKey);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
	 * Encrypt.
	 *
	 * @param valueToEnc the value to enc
	 * @param encKey the enc key
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String encrypt(String valueToEnc, String encKey) throws Exception {
		return encrypt(valueToEnc, encKey, false);
	}

	/**
	 * Encrypt.
	 *
	 * @param valueToEnc the value to enc
	 * @param encKey the enc key
	 * @param urlsafe the urlsafe
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String encrypt(String valueToEnc, String encKey, boolean urlsafe) throws Exception {
		if (org.apache.commons.lang.StringUtils.isEmpty(valueToEnc))
			return valueToEnc;
		Key key = generateKey(encKey.getBytes(StandardCharsets.UTF_8));
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encValue = c.doFinal(valueToEnc.replaceAll("^\"|\"$", "").getBytes());
		if (urlsafe)
			return Base64.encodeBase64URLSafeString(encValue);
		else
			return Base64.encodeBase64String(encValue);
	}

	/**
	 * Generate key.
	 *
	 * @param keyBytes the key bytes
	 * @return the key
	 * @throws Exception the exception
	 */
	private static Key generateKey(byte[] keyBytes) throws Exception {
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		return key;
	}

	/**
	 * Decrypt.
	 *
	 * @param valueToDeenc the value to deenc
	 * @param encKey the enc key
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String decrypt(String valueToDeenc, String encKey) throws Exception {
		Key key = generateKey(encKey.getBytes(StandardCharsets.UTF_8));

		byte[] content = Base64.decodeBase64(valueToDeenc.getBytes());
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] data = c.doFinal(content);
		String res = new String(data);
		return res;
	}
}
