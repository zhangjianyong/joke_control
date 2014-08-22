package com.doumiao.joke.coder;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public abstract class RSA {
	public static final String ALGORITHM = "RSA";
	private static final int KEY_SIZE = 1024;

	// private static final String DEFAULT_SEED =
	// "0f22507a10bbddd07d8a3082122966e3";

	public static void generate(String seed) throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance(ALGORITHM);
		// 初始化随机产生器
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed(seed.getBytes());
		keygen.initialize(KEY_SIZE, secureRandom);

		KeyPair keys = keygen.genKeyPair();

		DSAPublicKey publicKey = (DSAPublicKey) keys.getPublic();
		DSAPrivateKey privateKey = (DSAPrivateKey) keys.getPrivate();

		System.out.println("publickey:"
				+ (new BASE64Encoder()).encodeBuffer(publicKey.getEncoded()));
		System.out.println("privatekey:"
				+ (new BASE64Encoder()).encodeBuffer(privateKey.getEncoded()));
	}

	@Deprecated
	public static byte[] encrypt(byte[] data, String pri) throws Exception {
		byte[] prikeyBytes = (new BASE64Decoder()).decodeBuffer(pri);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(prikeyBytes);
		KeyFactory prikeyFactory = KeyFactory.getInstance(ALGORITHM);
		Key privateKey = prikeyFactory.generatePrivate(pkcs8KeySpec);
		Cipher cipher = Cipher.getInstance(prikeyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	@Deprecated
	public static byte[] decrypt(byte[] data, String pub) throws Exception {
		byte[] keyBytes = (new BASE64Decoder()).decodeBuffer(pub);
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
		Cipher pubCipher = Cipher.getInstance(keyFactory.getAlgorithm());
		pubCipher.init(Cipher.DECRYPT_MODE, publicKey);
		return pubCipher.doFinal(data);
	}

	/**
	 * 初始化私钥
	 * 
	 * @param priStr
	 *            BASE64加密后的私钥字符串
	 * @return
	 */
	public static PrivateKey initPrivateKey(String priStr) {
		try {
			byte[] prikeyBytes = (new BASE64Decoder()).decodeBuffer(priStr);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
					prikeyBytes);
			KeyFactory prikeyFactory = KeyFactory.getInstance(ALGORITHM);
			return prikeyFactory.generatePrivate(pkcs8KeySpec);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 初始化公钥
	 * 
	 * @param pubStr
	 *            BASE64加密后的公钥字符串
	 * @return
	 */
	public static PublicKey initPublicKey(String pubStr) {
		try {
			byte[] keyBytes = (new BASE64Decoder()).decodeBuffer(pubStr);
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			return keyFactory.generatePublic(pubKeySpec);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用私钥加密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, PrivateKey privateKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	/**
	 * 用公钥解密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, PublicKey publicKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
}
