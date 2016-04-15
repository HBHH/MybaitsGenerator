package com.common.util;

public class CifpaySystemException extends RuntimeException {

	private static final long serialVersionUID = 19700101000000L;

	private int code = -1;// <10000就是正常的 2位模块代号+3位错误代号,如：10001用户crud[增加(Create)、查询(Retrieve)、更新(Update)和删除(Delete)]

	public int getCode() {
		return code;
	}

	public CifpaySystemException(int code, Throwable root) {
		super(root);
		this.code = code;
	}

	public CifpaySystemException(Throwable root) {
		super(root);
	}

	public CifpaySystemException(int code, String message, Throwable root) {
		super(message, root);
		this.code = code;
	}

	public CifpaySystemException(String message, Throwable root) {
		super(message, root);
	}

	public CifpaySystemException(int code, String message) {
		super(message);
		this.code = code;
	}

	public CifpaySystemException(String message) {
		super(message);
	}

	@Override
	public String toString() {
		return super.toString() + " [code=" + code + "]";
	}

	public static void main(String[] args) {
		CifpaySystemException dataPlatformException = new CifpaySystemException(1, "", null);
		System.out.println(dataPlatformException);
	}
}