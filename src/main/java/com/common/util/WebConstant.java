package com.common.util;

import java.nio.charset.Charset;
import java.security.Provider;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class WebConstant {
	private static final Logger LOG = LogManager.getLogger(WebConstant.class);

	public static final String DATABASE_DRIVER_CLASS_NAME  = "com.mysql.jdbc.Driver";
	public static final String LOGIN_NAME_BLACK_WORDS = ",admin,manage,test,develop,cto,ceo,cfo,cio,fuck,shit,sex,";
	public static final String RSA_PUBLIC_KEY="30819f300d06092a864886f70d010101050003818d0030818902818100db4b7a4b051ab2f9647778ee512bdf141fbbf1b430ee1c131154f5d2d4bb1385178b5aeaed2b5b6a1b438f2994e76cc96c9d491499d9b66f58099e0dcf0b0253e62059ad1c0c359d52ce3f120ad7f59601d7a262d44fa23db23847eed0409c1081c18634a532c5be7a8ff5a906647383bb863376cbee78bb4c233f11ccda8d530203010001";
	public static final String RSA_PRIVATE_KEY="30820279020100300d06092a864886f70d0101010500048202633082025f02010002818100db4b7a4b051ab2f9647778ee512bdf141fbbf1b430ee1c131154f5d2d4bb1385178b5aeaed2b5b6a1b438f2994e76cc96c9d491499d9b66f58099e0dcf0b0253e62059ad1c0c359d52ce3f120ad7f59601d7a262d44fa23db23847eed0409c1081c18634a532c5be7a8ff5a906647383bb863376cbee78bb4c233f11ccda8d53020301000102818100a9273755d6c2197bb5bb5254c1d80d63007780a1757cb5bb1f8b6173171496a1fadf4b6a1b376e741243b29268817ab0844f6ea7ad64fa0c38e4723e448e4163f7caf3f3333ea4f1e2af5343d55ea3032041a3a466ab9b7f7060a1bf0d0a6e14b35e8b930eadf682eb8d939be36e0bc083b75b2e98733c45fb5cda1f6315c381024100fc1ba3480bfc7420e7779b51ed3b7671fb3b87d8768fe015b5e31e2cc6ead865d57df04cbc1f463ea62e3d2f2c3a18dbb9cb346ce5533d77f828c20bdd5f8f5b024100deae28874cd241aa6733338dff89d17e83c51f92f1755db5adfb0d51d283872522b4f21073c95fbb9dafdfb9b484f1db6b8ce5f8bedaa688d988dce5371f136902410080b8c3d7e776d0fc73f2d7f52ec1abaa6af26c5c13bd46570f7595549b8411557d7a3e1590000fe7b105eeeb85136cd9d3b573611e7e2fe66f68b01b4abe9dfd0241009f041f4cff3ee645998ca6b430576a92bde676b5a857f7a355aa81c87c5c10962442b2d9acd590d799b38e403ef8f0fd8fbffe39b390d07bc95c42e22c8ab481024100d3e4b40ba1ab202eb8bcbb8616a1118bfc1580a54a506680732112908c81bc8b13d714ac4b4ccec08f0b4b112a7faee2f7b6beab342007538513da3a17f5e687";
	/**
	 * 关于页的，理应在程序里面设置，这里仅仅是保护性的默认值，因此不需要写配置文件里
	 */
	public static final int PAGE_NO_DEFAULT = 1;
	public static final int PAGE_SIZE_DEFAULT = 10;
	public static final int PAGE_SIZE_MAX = 10000;
	
	public static final String MD5_SALT = "r*1&U9-G";
	private static final RSAKey[] rsaKeyArr = new RSAKey[2];
	public static final Provider SECURITY_PROVIDER = new BouncyCastleProvider();
	
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final Charset CHARSET_OBJECT = Charset.forName(CHARSET_UTF8);
	
	private WebConstant() {
	}
	
	public static RSAPublicKey getRsaPublicKey() {
		try {
			if (rsaKeyArr[0] == null) {
				rsaKeyArr[0] = (RSAPublicKey) SecurityUtil.getPublicKey("RSA", RSA_PUBLIC_KEY, "hex");
			}
		} catch (Exception e) {
			LOG.error(e, e);
			LOG.error("common.rsaPublicKey=" + RSA_PUBLIC_KEY);
		}
		return (RSAPublicKey) rsaKeyArr[0];
	}
	
	public static RSAPrivateKey getRsaPrivateKey() {
		try {
			if (rsaKeyArr[1] == null) {
				rsaKeyArr[1] = (RSAPrivateKey) SecurityUtil.getPrivateKey("RSA", RSA_PRIVATE_KEY, "hex");
			}
		} catch (Exception e) {
			LOG.error(e, e);
			LOG.error("common.rsaPrivateKey=" + RSA_PRIVATE_KEY);
		}
		return (RSAPrivateKey) rsaKeyArr[1];
	}
	
	/*##########################add by 谢彩莲######################*/
	public static final String RESULT_SUCCESS = "SUCCESS";
	public static final String RESULT_FAIL = "FAIL";
	
	/**请求方式：post*/
	public static final String REQUEST_POST  = "POST";
	
	/**请求方式:get*/
	public static final String REQUEST_GET  = "GET";

}