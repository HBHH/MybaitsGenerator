package com.common.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 由于BASE64Decoder.java是sun公司内部的，不公开的API,因此有警告
 * 
 * http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/
 * CryptoSpec.html#AppA<br>
 * 注意，sun.misc包是Sun公司提供给内部使用的专用API，在java API文档中我们看不到任何有关BASE64影子，不建议使用<br>
 * UrlBase64,也就是将“+”和“/”换成了“-”和“_”符号，且不适用补位http://www.4ucode.com/Study/Topic/
 * 2116709<br>
 * <br>
 * http://wbzboy.iteye.com/blog/321880 再通过Base64或Hex来进行编码 先使用<br>
 * <br>
 * encodeURIComponent() 函数对中文进行编码，再加密试试。<br>
 * 解密后，使用 java.net.Decoder.decode(str, "UTF-8") 进行中文还原。<br>
 * <br>
 * http://blog.csdn.net/xiaoxin_hs/article/details/5699960
 * 
 * @author huangym
 * 
 */
public class SecurityUtil {
	private static final Logger LOG = LoggerFactory.getLogger(SecurityUtil.class);

	private SecurityUtil() {

	}

	public static void printSecurityProviders() {
		if (LOG.isDebugEnabled()) {
			Provider[] providerArr = Security.getProviders();
			int providerArrLength = (providerArr != null ? providerArr.length : 0);
			LOG.debug("providerArr：");
			for (int i = 0; i < providerArrLength; i++) {
				LOG.debug("providerArr[" + i + "].getName()=" + providerArr[i].getName() + " - version:" + providerArr[i].getVersion());
				LOG.debug(providerArr[i].getInfo());
				LOG.debug("----------------------------------------");
			}
			LOG.debug("****************************************");
			LOG.debug("MessageDigest:");
			for (String s : Security.getAlgorithms("MessageDigest")) {
				LOG.debug(s);
			}
			LOG.debug("****************************************");
			LOG.debug("KeyPairGenerator：");
			for (String s : Security.getAlgorithms("KeyPairGenerator")) {
				LOG.debug(s);
			}
		}
	}

	/**
	 * 
	 * @param algorithm
	 *            加密算法:RSA DES DESede 等
	 * @param blockMode
	 *            反馈模式:<br>
	 *            ECB CBC CFB OFB CTR<br>
	 *            ECB|CBC|PCBC|CTR|CTS|CFB|OFB<br>
	 *            CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64<br>
	 *            OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64<br>
	 *            CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128<br>
	 *            OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128<br>
	 *            www.cnblogs.com/happyhippy/archive/2006/12/23/601353.html<br>
	 *            cr.openjdk.java.net/~jjg/7064075/src/share/classes/com/sun/
	 *            crypto/provider/SunJCE.java.patch<br>
	 * 
	 * @param blockPad
	 *            填充算法:<br>
	 *            java:NoPadding ISO10126Padding
	 *            OAEPPadding(OAEPWithMD5AndMGF1Padding
	 *            ,OAEPWithSHA-512AndMGF1Padding) PKCS1Padding PKCS5Padding
	 *            SSL3Padding <br>
	 * <br>
	 *            .net:ANSIX923 ISO10126 None PKCS7 Zeros <br>
	 *            简单对比之下发现，通用的有None，ISO10126两种填充法，
	 *            实际上PKCS5Padding与PKCS7Padding基本上也是可以通用的<br>
	 *            www.cnblogs.com/midea0978/articles/1437257.html<br>
	 * <br>
	 *            cr.openjdk.java.net/~jjg/7064075/src/share/classes/com/sun/
	 *            crypto/provider/SunJCE.java.patch<br>
	 *            NOPADDING|PKCS5PADDING|ISO10126PADDING
	 * @return
	 * @throws Exception
	 */
	public static Cipher getCipher(String algorithm, Provider provider, String blockMode, String blockPad) {
		String transformation = "";
		if (StrUtil.isBlank(algorithm)) {
			throw new NullPointerException("algorithm==null");
		}
		if (StrUtil.isBlank(blockMode) && StrUtil.isBlank(blockPad)) {
			transformation = algorithm;
		} else {
			if (!StrUtil.isBlank(blockMode)) {
				transformation = transformation + algorithm + "/" + blockMode;
			} else {
				transformation = transformation + algorithm + "/";
			}
			if (!StrUtil.isBlank(blockPad)) {
				transformation = transformation + "/" + blockPad;
			} else {
				transformation = transformation + "/";
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("transformation=" + transformation);
		}
		try {
			if (provider != null) {
				return Cipher.getInstance(transformation, provider);
			} else {
				return Cipher.getInstance(transformation);
			}
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
	}

	/**
	 * 产生密匙对
	 * 
	 * @param algorithm
	 *            RSA or DSA
	 * @param keySize
	 *            这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低.最好是1024以上[1024以下已经被破解] 2048
	 *            取值范围：512, 1024, 2048, 4096
	 * @param passwordByteArr
	 *            如果指定这个值,任何时候生成的密匙对都是固定的,防止密匙文件丢失后,只要通过这个就可以重新生成密匙
	 * @return 密匙对KeyPair
	 * @throws Exception
	 */
	public static KeyPair generateKey(String algorithm, Provider provider, int keySize, byte[] passwordByteArr) {
		KeyPairGenerator keyPairGenerator = null;
		try {
			if (provider != null) {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm, provider);
			} else {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			}
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
		SecureRandom secureRandom;
		if (passwordByteArr != null && passwordByteArr.length > 0) {
			secureRandom = new SecureRandom(passwordByteArr);
		} else {
			secureRandom = new SecureRandom();
		}
		keyPairGenerator.initialize(keySize, secureRandom);
		KeyPair keyPair = keyPairGenerator.genKeyPair();// 生成密钥组
		return keyPair;
	}

	/**
	 * 产生密匙对
	 * 
	 * @param algorithm
	 * @param keySize
	 * @param passwordByteArr
	 *            如果指定这个值,任何时候生成的密匙对都是固定的,防止密匙文件丢失后,只要通过这个就可以重新生成密匙
	 * @param keyEncode
	 *            hex,base64,BigInteger
	 * @return 返回指定编码的字符串 公匙 私匙
	 * @throws Exception
	 */
	public static String[] generateKeyStr(String algorithm, Provider provider, int keySize, final byte[] passwordByteArr, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}

		KeyPair keyPair = generateKey(algorithm, provider, keySize, passwordByteArr);

		if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
			return new String[] { ByteAndHexUtil.bytesToHexs(keyPair.getPublic().getEncoded()), ByteAndHexUtil.bytesToHexs(keyPair.getPrivate().getEncoded()) };
		} else if (keyEncode.indexOf("base64") > -1) {
			try {
				return new String[] { new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()), "UTF-8"), new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()), "UTF-8") };
			} catch (Exception e) {
			}
		} else if (keyEncode.indexOf("bigint") > -1) {
			return new String[] { String.valueOf(new BigInteger(keyPair.getPublic().getEncoded())), String.valueOf(new BigInteger(keyPair.getPrivate().getEncoded())) };
		}

		return null;
	}

	/**
	 * 根据指定的编码,转化公匙
	 * 
	 * @param publicKey
	 * @param keyEncode
	 *            hex,base64,BigInteger
	 * @return 返回指定编码的字符串
	 * @throws Exception
	 */
	public static String getPublicKeyStr(PublicKey publicKey, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
			return ByteAndHexUtil.bytesToHexs(publicKey.getEncoded());
		} else if (keyEncode.indexOf("base64") > -1) {
			try {
				return new String(Base64.encodeBase64(publicKey.getEncoded()), "UTF-8");
			} catch (Exception e) {
			}
		} else if (keyEncode.indexOf("bigint") > -1) {
			return String.valueOf(new BigInteger(publicKey.getEncoded()));
		}
		return null;
	}

	/**
	 * 根据指定的编码,转化公匙
	 * 
	 * @param algorithm
	 * @param publicKeyStr
	 * @param keyEncodeOld
	 *            hex,base64,BigInteger
	 * @param keyEncodeNew
	 *            hex,base64,BigInteger
	 * @return 返回指定编码的字符串
	 * @throws Exception
	 */
	public static String getPublicKeyStr(String algorithm, String publicKeyStr, String keyEncodeOld, String keyEncodeNew) {
		PublicKey publicKey = getPublicKey(algorithm, publicKeyStr, keyEncodeOld);
		return getPublicKeyStr(publicKey, keyEncodeNew);
	}

	public static String getPrivateKeyStr(PrivateKey privateKey, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
			return ByteAndHexUtil.bytesToHexs(privateKey.getEncoded());
		} else if (keyEncode.indexOf("base64") > -1) {
			try {
				return new String(Base64.encodeBase64(privateKey.getEncoded()), "UTF-8");
			} catch (Exception e) {
			}
		} else if (keyEncode.indexOf("bigint") > -1) {
			return String.valueOf(new BigInteger(privateKey.getEncoded()));
		}
		return null;
	}

	public static String getPrivateKeyStr(String algorithm, String privateKeyStr, String keyEncodeOld, String keyEncodeNew) {
		PrivateKey privateKey = getPrivateKey(algorithm, privateKeyStr, keyEncodeOld);
		return getPrivateKeyStr(privateKey, keyEncodeNew);
	}

	/**
	 * @param algorithm
	 * @param publicKeyStr
	 *            公匙的字符串形式
	 * @param keyEncode
	 *            base64,hex,BigInteger
	 * @return
	 */
	public static PublicKey getPublicKey(String algorithm, String publicKeyStr, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		X509EncodedKeySpec keySpec = null;
		try {
			if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
				keySpec = new X509EncodedKeySpec(ByteAndHexUtil.hexsToBytes(publicKeyStr));
			} else if (keyEncode.indexOf("base64") > -1) {
				keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyStr.getBytes("UTF-8")));
			} else if (keyEncode.indexOf("bigint") > -1) {
				keySpec = new X509EncodedKeySpec((new BigInteger(publicKeyStr)).toByteArray());
			}
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			return keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param algorithm
	 * @param privateKeyStr
	 *            私匙的字符串形式
	 * @param keyEncode
	 *            base64,hex,BigInteger
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String algorithm, String privateKeyStr, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		PKCS8EncodedKeySpec keySpec = null;
		try {
			if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
				keySpec = new PKCS8EncodedKeySpec(ByteAndHexUtil.hexsToBytes(privateKeyStr));
			} else if (keyEncode.indexOf("base64") > -1) {
				keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyStr.getBytes("UTF-8")));
			} else if (keyEncode.indexOf("bigint") > -1) {
				keySpec = new PKCS8EncodedKeySpec((new BigInteger(privateKeyStr)).toByteArray());
			}
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			return keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
	}

	public static Signature getSignature(String algorithm, Provider provider) {
		if (algorithm == null || "".equals(algorithm.trim())) {
			throw new NullPointerException("algorithm=" + algorithm);
		}
		try {
			if (provider != null) {
				return Signature.getInstance(algorithm);
			} else {
				return Signature.getInstance(algorithm, provider);
			}
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param serviceType
	 *            MessageDigest||?
	 * @return
	 */
	public static String[] getAlgorithms(String serviceType) {//
		Set<String> result = new HashSet<String>();

		// All all providers
		Provider[] providerArr = Security.getProviders();
		for (int i = 0, providerArrLength = (providerArr != null ? providerArr.length : 0); i < providerArrLength; i++) {
			// Get services provided by each provider
			Set<Object> keyset = providerArr[i].keySet();
			String key = "";
			for (Iterator<Object> it = keyset.iterator(); it.hasNext();) {
				key = (String) it.next();
				key = key.split("   ")[0];
				if (StrUtil.isBlank(serviceType)) {
					result.add(key);
				} else {
					if (key.startsWith(serviceType + ".")) {
						result.add(key.substring(serviceType.length() + 1).split(" ")[0]);
					} else if (key.startsWith("Alg.Alias. " + serviceType + ".")) {
						// This is an alias
						result.add(key.substring(serviceType.length() + 11).split(" ")[0]);
					}
				}
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * 产生密匙
	 * 
	 * @param algorithm
	 *            加密算法,可用 DES,AES,DESede,Blowfish,RC2<br>
	 *            DES算法必须是56位 DESede算法可以是112位或168位 AES算法可以是128、192、256位
	 * @param provider
	 * 
	 * @param keySize
	 *            这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低.最好是1024以上[1024以下已经被破解] 2048
	 *            取值范围：512, 1024, 2048, 4096
	 * @param passwordByteArr
	 *            如果指定这个值,任何时候生成的密匙对都是固定的,防止密匙文件丢失后,只要通过这个就可以重新生成密匙
	 * @return
	 * @throws Exception
	 */
	public static SecretKey generateKeySymmetric(String algorithm, Provider provider, int keySize, byte[] passwordByteArr) {

		KeyGenerator keyGenerator = null;
		try {
			if (provider != null) {
				keyGenerator = KeyGenerator.getInstance(algorithm, provider);
			} else {
				keyGenerator = KeyGenerator.getInstance(algorithm);
			}
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
		SecureRandom secureRandom;
		if (passwordByteArr != null && passwordByteArr.length > 0) {
			secureRandom = new SecureRandom(passwordByteArr);
		} else {
			secureRandom = new SecureRandom();
		}
		keyGenerator.init(keySize, secureRandom);
		return keyGenerator.generateKey();
	}

	public static String generateKeyStrSymmetric(String algorithm, Provider provider, int keySize, byte[] passwordByteArr, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		SecretKey secretKey = generateKeySymmetric(algorithm, provider, keySize, passwordByteArr);
		if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
			return ByteAndHexUtil.bytesToHexs(secretKey.getEncoded());
		} else if (keyEncode.indexOf("base64") > -1) {
			try {
				return new String(Base64.decodeBase64(secretKey.getEncoded()), "UTF-8");
			} catch (Exception e) {
			}
		} else if (keyEncode.indexOf("bigint") > -1) {
			return String.valueOf(new BigInteger(secretKey.getEncoded()));
		}
		return null;
	}

	// hmacSHA256
	public static SecretKey getSecretKey(String algorithm, String secretKeyStr, String keyEncode) {
		if (keyEncode != null) {
			keyEncode = keyEncode.toLowerCase();
		}
		SecretKeySpec secretKeySpec = null;
		try {
			if (keyEncode == null || "".equals(keyEncode) || keyEncode.indexOf("hex") > -1) {
				secretKeySpec = new SecretKeySpec(ByteAndHexUtil.hexsToBytes(secretKeyStr), algorithm);
			} else if (keyEncode.indexOf("base64") > -1) {
				secretKeySpec = new SecretKeySpec(Base64.decodeBase64(secretKeyStr.getBytes("UTF-8")), algorithm);
			} else if (keyEncode.indexOf("bigint") > -1) {
				secretKeySpec = new SecretKeySpec((new BigInteger(secretKeyStr)).toByteArray(), algorithm);
			}
			return secretKeySpec;
		} catch (Exception e) {
			throw new CifpaySystemException(e.getMessage(), e);
		}
	}
}