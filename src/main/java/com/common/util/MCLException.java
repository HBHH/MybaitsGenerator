package com.common.util;


/**
 * 物信证异常类
 * @Title:    MCLException
 * Company:   cifpay
 * Copyright: Copyright(C) 2013
 * @Version   1.0
 * @author    chenbin
 * @date:     2015年6月3日
 * @time:     上午11:16:06
 * @Description:
 */
public class MCLException extends RuntimeException {

	private static final long serialVersionUID = -2843267860767693236L;

	private int code = 500;

	public int getCode() {
		return code;
	}

	public MCLException(int code, Throwable root) {
		super(root);
		this.code = code;
	}

	public MCLException(Throwable root) {
		super(root);
	}

	public MCLException(int code, String message, Throwable root) {
		super(message, root);
		this.code = code;
	}

	public MCLException(String message, Throwable root) {
		super(message, root);
	}

	public MCLException(int code, String message) {
		super(message);
	}

	public MCLException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return super.toString() + " [code=" + code + "]";
	}

}
