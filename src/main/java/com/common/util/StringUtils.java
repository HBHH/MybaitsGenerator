package com.common.util;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @Title:    StringUtils
 * Company:   cifpay
 * Copyright: Copyright(C) 2013
 * @Version   1.0
 * @author    chenbin
 * @date:     2015年6月3日
 * @time:     上午11:14:03
 * @Description:
 */
public class StringUtils {
	private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);
	private static final String RANDOM_DEFAULT_STR = "123456789abcdefghijklmnopqrstuvwxyz";
	private static final String RANDOM_DEFAULT_CAPTCHA_STR = "123456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";

	private StringUtils() {
	}

	/**
	 * 建议直接调用 对应类型判断空函数，因为这个还要先判断参数真正的类型
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(final Object s) {
		if (s == null) {
			return true;
		}
		if (s instanceof String) {
			return isEmptyString((String) s);
		}
		if (s instanceof Set) {
			return isEmptySet((Set<?>) s);
		}
		if (s instanceof List<?>) {
			return isEmptyList((List<?>) s);
		}
		if (s.getClass().isArray()) {
			return isEmptyArray((Object[]) s);
		}
		return false;
	}

	public static boolean isEmptyString(final String s) {
		return (s == null || s.length() == 0);
	}

	public static boolean isEmptySet(final Set<?> set) {
		return (set == null || set.isEmpty());
	}

	public static boolean isEmptyList(final List<?> list) {
		return (list == null || list.isEmpty());
	}

	public static boolean isEmptyArray(final Object[] objectArray) {
		return (objectArray == null || objectArray.length == 0);
	}

	public static boolean isEmptyArray(final byte[] objectArray) {
		return (objectArray == null || objectArray.length == 0);
	}

	/**
	 * 自动去掉前后空格
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s) {
		if (s == null || s.trim().length() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 首字母大写
	 * 
	 * @param str
	 */
	public static String UpperHeadLetter(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}


	/**
	 * 去掉前后的空白字符，包括 空格 tab 前后的换行
	 * 
	 * @param s
	 * @return
	 */
	public static String trimLineEnterBlank(String s) {
		if (s == null || "".equals(s.trim())) {
			return "";
		}
		s = s.trim();
		while (s.startsWith("	")) {
			s = s.substring(1);
		}
		while (s.endsWith("	")) {
			s = s.substring(0, s.length() - 1);
		}

		while (s.startsWith("\r")) {
			s = s.substring(1);
		}
		while (s.startsWith("\n")) {
			s = s.substring(1);
		}
		while (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		while (s.endsWith("\r")) {
			s = s.substring(0, s.length() - 1);
		}

		while (s.startsWith("	")) {
			s = s.substring(1);
		}
		while (s.endsWith("	")) {
			s = s.substring(0, s.length() - 1);
		}
		return s.trim();
	}

	/**
	 * 功能:null对象转换为空字符串,前后空格不去掉
	 * 
	 * @param s
	 * @param strDefault
	 * @return
	 */
	public static String nullToStr(String s) {
		return s != null ? s : "";
	}

	public static String urlEncode(String s, String charset) {
		if (isBlank(s)) {
			return "";
		}
		if (isBlank(charset)) {
			charset = WebConstant.CHARSET_UTF8;
		}
		try {
			return URLEncoder.encode(s.trim(), charset);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return s;
	}

	public static String urlEncode(String s) {
		return urlEncode(s, "");
	}

	public static String urlDecode(String s, String charset) {
		if (isBlank(s)) {
			return "";
		}
		if (isBlank(charset)) {
			charset = WebConstant.CHARSET_UTF8;
		}
		try {
			return URLDecoder.decode(s, charset);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return s;
	}

	/**
	 * 功能：格式化输出标题等内容
	 * 
	 * @param s
	 * @param lengthLeave
	 * @param suffix
	 * @return
	 */
	public static String format(String s, int lengthLeave, String suffix) {
		if (isBlank(s) || lengthLeave < 1) {
			return "";
		}
		if (s.length() > lengthLeave) {
			s = s.substring(0, lengthLeave > 2 ? (lengthLeave - 2) : 0);
			if (suffix == null) {
				s = s + "...";
			} else {
				s = s + suffix;
			}
		}
		return s;
	}

	public static String convertEncode(String s, String charsetOrigin, String charsetDestination) {
		if (isBlank(s)) {
			return s;
		}
		if (isBlank(charsetOrigin) && isBlank(charsetDestination)) {
			return s;
		}
		try {
			byte[] sByte;
			if (charsetOrigin == null || "".equals(charsetOrigin)) {
				sByte = s.getBytes(WebConstant.CHARSET_UTF8);
			} else {
				sByte = s.getBytes(charsetOrigin); // "ISO8859_1"
			}
			if (charsetDestination == null || "".equals(charsetDestination)) {
				s = new String(sByte,WebConstant.CHARSET_UTF8);
			} else {
				s = new String(sByte, charsetDestination);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return s;
	}

	public static String toNotNullAndTrim(String s) {
		if (s == null) {
			return "";
		}
		s = s.trim();
		return s;
	}

	public static String toNotNullAndTrim(Cell cell) {
		if (cell == null) {
			return "";
		}
		return toNotNullAndTrim(cell.toString());
	}

	/**
	 * 功能：替换特殊字符，不替换&符号，方便前台使用js中的toHtmlTxt()函数在客户端进行替换，减轻服务器的负担。
	 * 这样会导致toHtmlTag方法后，文本中的&lt;|&gt;|&quot;|&#039; 将会替换成：<|>|"|'
	 * 
	 * @param s
	 * @return
	 */
	public static String toHtmlTxt(String s) {
		if (s == null) {
			return "";
		}
		s = s.trim();
		int index = s.indexOf('<');
		if (index < 0) {
			index = s.indexOf('>');
			if (index < 0) {
				index = s.indexOf('\"');
				if (index < 0) {
					index = s.indexOf('\'');
				}
			}
		}
		if (index < 0) {// 没有任何特殊字符
			return s;
		}

		StringBuilder resultSb = null;
		char[] origin = null;
		int beg = 0, len = s.length();

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			// 1.default就是如果没有符合的case就执行它,default并不是必须的.
			// 2.case后的语句可以不用大括号.
			// 3.switch语句的判断条件可以接受int,byte,char,short,不能接受其他类型.
			// 4.一旦case匹配,就会顺序执行后面的程序代码,而不管后面的case是否匹配,直到遇见break,利用这一特性可以让好几个case执行统一语句.
			// 要是文本中 有这个的话"&lt;" 反转换的话会转换为"<"
			switch (c) {
			case 0:
			case '<':
			case '>':
			case '\"':
			case '\'': {
				if (resultSb == null) {
					origin = s.toCharArray();
					resultSb = new StringBuilder(len + 10);
				}
				if (i > beg) {
					resultSb.append(origin, beg, i - beg);
				}
				beg = i + 1;
				// //System.out.println("i=" + i + " c=" + c + " beg=" + beg+
				// " results=" + results.toString());
				switch (c) {
				default:
					continue;
				case '<':
					resultSb.append("&lt;");
					break;
				case '>':
					resultSb.append("&gt;");
					break;
				case '\"':
					resultSb.append("&quot;");
					break;
				case '\'':
					resultSb.append("&#039;");
					break;
				}
				break;
			}
			}
		}
		if (resultSb == null) {
			return s;
		}
		resultSb.append(origin, beg, len - beg);
		return resultSb.toString();
	}

	/**
	 * 功能：该函数最好不要直接转换 非管理员的输入内容最好不要使用 否则直接转换的 script代码会执行
	 * 要是一定要转换的话，最好最后用scriptToHtml()方法来注释掉script代码
	 * 如：<%=scriptToHtml(htmlDecode(s))%>
	 * 
	 * @param s
	 * @return
	 */
	public static String toHtmlTag(String s) {
		if (s == null || s.equals(""))
			return "";

		int index = s.indexOf("&lt;");
		if (index > -1) {
			s = s.replaceAll("&lt;", "<");
			index = s.indexOf("&gt;");
			if (index > -1) {
				s = s.replaceAll("&gt;", ">");
				index = s.indexOf("&quot;");
				if (index > -1) {
					s = s.replaceAll("&quot;", "\"");
					index = s.indexOf("&#039;");
					if (index > -1) {
						s = s.replaceAll("&#039;", "\'");
					}
				}
			}
		}
		return s;
	}

	/**
	 * 忽略大小写替换
	 * 
	 * @param source
	 * @param oldstring
	 * @param newstring
	 * @return
	 */

	public static String scriptTagToHtmlTxt(String s) {
		if (s == null) {
			return "";
		}
		if (s.length() < 8) {
			return s;
		}
		return ignoreCaseReplace(ignoreCaseReplace(s, "<script", "&lt;&nbsp;script"), "</script>", "&lt;&nbsp;/script&gt;");
	}

	public static String ignoreCaseReplace(String source, String sOrigin, String sDestination) {
		Pattern pattern = Pattern.compile(sOrigin, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(source);
		String result = matcher.replaceAll(sDestination);
		return result;
	}

	/**
	 * 功能：获取随机字符串
	 * 
	 * @param sRandom
	 *            随机字符串的范围
	 * @param resultLength
	 *            返回字符的个数
	 * @return String
	 */
	public static String getRand(String sRandom, int resultLength) {
		StringBuilder resultSb = new StringBuilder();
		Random random = new Random();
		if (sRandom == null || sRandom.length() == 0) {
			sRandom = RANDOM_DEFAULT_STR;
		}
		char chars[] = sRandom.toCharArray();
		for (int i = 0; i < resultLength; i++) {
			resultSb.append(chars[random.nextInt(chars.length)]);
		}
		return resultSb.toString();
	}

	public static String getRand(int stringLong) {
		return getRand("", stringLong);
	}

	public static String getRandCaptchaValue(int stringLong) {
		return getRand(RANDOM_DEFAULT_CAPTCHA_STR, stringLong);
	}

	public static boolean isChar(String s) {
		if (isBlank(s)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z]*$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	public static boolean isNumber(String s) {
		if (isBlank(s)) {
			return false;
		}
		// Pattern pattern = Pattern.compile("^[0-9]*$");
		// Matcher matcher = pattern.matcher(s);
		// return matcher.matches();

		// return Pattern.matches("\\d+", s);比当前方法慢5倍左右
		boolean result = true;
		char[] sCharArr = s.toCharArray();
		int sCharArrLength = sCharArr.length;
		if (sCharArrLength == 0) {
			return false;
		}
		for (int i = 0; i < sCharArrLength; i++) {
			if (!Character.isDigit(sCharArr[i])) {
				return false;
			}
		}
		return result;
	}

	public static boolean isCharAndNumber(String s) {
		if (isBlank(s)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	/**
	 * 功能：首字符不能为数字 不能有除字符数字以外的任何字符
	 * 
	 * @param s
	 * @return
	 */
	public boolean isMemberId(String s) {
		if (isBlank(s)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9]*$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	public static boolean isSimpleChar(String s) {
		if (isBlank(s)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\-_]*$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	/**
	 * 功能：一个中文字符算两个字符
	 * 
	 * @param s
	 * @return
	 */
	public static int getLength(String s) {
		if (s == null) {
			return 0;
		}
		int sLength = s.length();
		int result = 0;
		for (int i = 0; i < sLength; i++) {
			result = result + (s.charAt(i) > 255 ? 2 : 1);
		}
		return result;
	}
	
	public static String toFirstUpperCases(String s) {// 把一个字符串的首字母变大写 中间有_
		if (isBlank(s)) {
			return s;
		}
		StringBuffer sbuf=new StringBuffer();
		String [] ms=s.split("_");
		if(ms==null || ms.length==0){
			return s;
		}
		for(String m:ms){
			sbuf.append(toFirstUpperCase(m));
		}
		
		return sbuf.toString();
	}
	
	

	public static String toFirstUpperCase(String s) {// 把一个字符串的首字母变大写
		if (isBlank(s)) {
			return s;
		}
		// A 65 a 97 Z 90 z 122
		int charInt = (int) s.charAt(0);
		if (charInt > 96 && charInt < 123) {// 是小写字母 是小写字母才转化
			return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
		}
		return s;
	}

	public static String toFirstLowerCase(String s) {// 把一个字符串的首字母变小写
		if (isBlank(s)) {
			return s;
		}
		// A 65 a 97 Z 90 z 122
		int charInt = (int) s.charAt(0);
		if (charInt > 64 && charInt < 91) {// 是大写字母
			return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
		}
		return s;
	}

	public static String dropHtmlTag(String s) {
		if (isBlank(s)) {
			return s;
		}
		s = s.replaceAll("</?[^>]+>", ""); // 剔出了<html>的标签
		return s;
	}

	public static String getTextAreaHtml(String s) {
		if (s == null || "".equals(s)) {
			return "";
		}
		s = s.replaceAll("\r\n", "<br/>");
		s = s.replaceAll("\r", "<br/>");
		s = s.replaceAll("\n", "<br/>");
		s = s.replaceAll(" ", "&nbsp;");
		return s;
	}

	/**
	 * 功能:去掉字符串前后的 某个特殊字符
	 * 
	 * @param s
	 * @param sDelete
	 * @return
	 */
	public static String trim(String s, String sDelete) {
		if (s == null || s.equals("")) {
			return "";
		}
		if (sDelete == null || sDelete.equals("")) {
			return s;
		}

		int charStrLength = sDelete.length();
		while (s.startsWith(sDelete)) {
			s = s.substring(charStrLength);
		}
		while (s.endsWith(sDelete)) {
			s = s.substring(0, s.length() - charStrLength);
		}
		return s;
	}

	public static String escape(String s) {
		int i;
		char j;
		StringBuilder resultSb = new StringBuilder();
		resultSb.ensureCapacity(s.length() * 6);
		for (i = 0; i < s.length(); i++) {
			j = s.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
				resultSb.append(j);
			else if (j < 256) {
				resultSb.append("%");
				if (j < 16)
					resultSb.append("0");
				resultSb.append(Integer.toString(j, 16));
			} else {
				resultSb.append("%u");
				resultSb.append(Integer.toString(j, 16));
			}
		}
		return resultSb.toString();
	}

	public static String unescape(String s) {
		StringBuilder resultSb = new StringBuilder();
		resultSb.ensureCapacity(s.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < s.length()) {
			pos = s.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (s.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(s.substring(pos + 2, pos + 6), 16);
					resultSb.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(s.substring(pos + 1, pos + 3), 16);
					resultSb.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					resultSb.append(s.substring(lastPos));
					lastPos = s.length();
				} else {
					resultSb.append(s.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return resultSb.toString();
	}

	/**
	 * 功能:替换特殊的字符,java本身的replace无法替换一些特殊的字符
	 * 
	 * @param s
	 * @param sOrigin
	 * @param sDestination
	 * @param type
	 *            1=replaceFirst 2=replaceLast 3||其它=replaceAll
	 * @return
	 */
	public static String replace(String s, String sOrigin, String sDestination, int type) {
		if (s == null || s.equals(""))
			return s;

		if (sOrigin == null || "".equals(sOrigin) || sDestination == null || sOrigin.equals(sDestination)) {
			return s;
		}
		String result = "";
		int intFromLen = sOrigin.length();
		int intPos;
		if (type == 1) {
			if ((intPos = s.indexOf(sOrigin)) != -1) {
				result = result + s.substring(0, intPos);
				result = result + sDestination;
				s = s.substring(intPos + intFromLen);
			}
		} else if (type == 2) {
			if ((intPos = s.lastIndexOf(sOrigin)) != -1) {
				result = result + s.substring(0, intPos);
				result = result + sDestination;
				s = s.substring(intPos + intFromLen);
			}
		} else {
			while ((intPos = s.indexOf(sOrigin)) != -1) {
				result = result + s.substring(0, intPos);
				result = result + sDestination;
				s = s.substring(intPos + intFromLen);
			}
		}

		result = result + s;
		return result;
	}

	public static String replaceFirst(String s, String sOrigin, String sDestination) {
		return replace(s, sOrigin, sDestination, 1);
	}

	public static String replaceLast(String s, String sOrigin, String sDestination) {
		return replace(s, sOrigin, sDestination, 2);
	}

	public static String replaceAll(String s, String sOrigin, String sDestination) {
		return replace(s, sOrigin, sDestination, 3);
	}

	public static String toClassName(String s) {// 把一个字符串 转换为类名的规则 首字母大写
		// "- _字母" 转换为"大写字母"
		if (isBlank(s)) {
			throw new NullPointerException("");
		}
		s = s.toLowerCase();
		s = s.trim().replaceAll(" ", "").replaceAll("　", "");
		// 去掉前后的下 划线,美元符号 中杠字符 因为这些都是不标准的写法
		while (s.startsWith("_") || s.startsWith("-") || s.startsWith("$")) {
			s = s.substring(1);
		}
		while (s.endsWith("_") || s.endsWith("-") || s.startsWith("$")) {
			if (s.length() > 1) {
				s = s.substring(0, s.length() - 1);
			} else {
				return "";
			}
		}
		if ("".equals(s)) {
			return "";
		}
		if (s.length() == 1) {
			return s.toUpperCase();
		}
		int index = s.indexOf('_');
		String tempStr = "";
		if (index < 1) {// 没有
			return toFirstUpperCase(s);
		} else {// 处理所有的下划线 把紧跟着他后的字母转换大写 后面肯定是有内容的
			tempStr = toFirstUpperCase(s.substring(0, index));
			s = tempStr + toFirstUpperCase(toClassName(s.substring(index + 1)));
		}
		index = s.indexOf('-');
		if (index < 1) {// 没有
			return toFirstUpperCase(s);
		} else {// 处理所有的下划线 把紧跟着他后的字母转换大写 后面肯定是有内容的
			tempStr = toFirstUpperCase(s.substring(0, index));
			s = tempStr + toFirstUpperCase(toClassName(s.substring(index + 1)));
		}
		index = s.indexOf('$');
		if (index < 1) {// 没有
			return toFirstUpperCase(s);
		} else {// 处理所有的下划线 把紧跟着他后的字母转换大写 后面肯定是有内容的
			tempStr = toFirstUpperCase(s.substring(0, index));
			return tempStr + toFirstUpperCase(toClassName(s.substring(index + 1)));
		}
	}

	public static String toClassPropertyName(String s) {// 把一个字符串 转换为类变量名的规则
		return toFirstLowerCase(toClassName(s));
	}

	public static Blob toBlob(String s) {
		if (s == null || "".equals(s)) {
			return null;
		} else {
			try {
				return new SerialBlob(s.getBytes(WebConstant.CHARSET_UTF8));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			return null;
		}
	}

	public static Blob toClob(String s) {
		if (s == null || "".equals(s)) {
			return null;
		} else {
			try {
				return new SerialBlob(s.getBytes(WebConstant.CHARSET_UTF8));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			return null;
		}
	}

	public static URL toUrl(String s) {
		if (s == null || "".equals(s)) {
			throw new NullPointerException("s==null");
		}
		try {
			return new URL(s);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public static boolean toBoolean(String s, boolean defaultValue) {
		boolean result = defaultValue;
		if (isBlank(s)) {
			return false;
		}
		s = s.trim();
		if ("false".equals(s) || "".equals(s) || "0".equals(s)) {
			return false;
		} else if ("true".equals(s) || "1".equals(s)) {
			return true;
		}
		return result;
	}

	public static boolean toBoolean(String s) {
		return toBoolean(s, false);
	}

	public static boolean toBoolean(int booleanInt) {
		if (booleanInt < 1) {
			return false;
		} else {
			return true;
		}
	}

	public static byte toByte(String s) {
		return toByte(s, (byte) 0);
	}

	public static byte toByte(String s, byte defaultValue) {
		byte byteTemp = defaultValue;
		s = MathUtil.parseAvgFormat(s);
		try {
			byteTemp = Byte.parseByte(s);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return byteTemp;
	}

	public static short toShort(String s) {
		return toShort(s, (short) 0);
	}

	public static short toShort(String s, short defaultValue) {
		short shortTemp = defaultValue;
		s = MathUtil.parseAvgFormat(s);
		try {
			shortTemp = Short.parseShort(s);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return shortTemp;
	}

	public static char toChar(String s) {
		return toChar(s, ' ');
	}

	public static char toChar(String s, char defaultValue) {
		char charTemp = defaultValue;
		try {
			if (s.length() > 1) {
				s = s.substring(0, 1);
			}
			if (s.length() == 1) {
				charTemp = s.charAt(0);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return charTemp;
	}

	public static int isInArr(Object[] arr, Object value) {
		int arrLength = arr != null ? arr.length : 0;
		if (arrLength < 1) {
			return -1;
		}
		if (value == null) {
			for (int i = 0; i < arrLength; i++) {
				if (arr[i] == null) {
					return i;
				}
			}
			return -1;
		}
		if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
			String valueStr = value.toString();
			for (int i = 0; i < arrLength; i++) {
				if (valueStr.equals(arr[i])) {
					return i;
				}
			}
			return -1;
		}

		for (int i = 0; i < arrLength; i++) {
			if (value.equals(arr[i])) {
				return i;
			}
		}
		return -1;
	}

	public static int isInArr(String[] arr, String value) {
		int arrLength = arr != null ? arr.length : 0;
		if (arrLength < 1) {
			return -1;
		}
		if (value == null) {
			for (int i = 0; i < arrLength; i++) {
				if (arr[i] == null) {
					return i;
				}
			}
			return -1;
		}

		for (int i = 0; i < arrLength; i++) {
			if (value.equals(arr[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 该方法会自动的把html标签转化为&...等符号,建议项目中只要获取字符串参数的，<br>
	 * 都应该使用该方法,前后的空白字符会被默认去掉
	 * 
	 * @param s
	 * @return
	 */
	public static String getParameter(String s) {
		if (StringUtils.isBlank(s)) {
			return "";
		}
		// try {
		// s = new String(s.getBytes("ISO8859_1"), WebConstant.CHARSET_UTF8);
		// } catch (Exception e) {
		// }
		s = toHtmlTxt(s);
		return s;
	}

	public static String reverse(String s) {
		if (StringUtils.isBlank(s)) {
			return null;
		}
		return (new StringBuilder(s)).reverse().toString();
	}

	public static String formatInt(int length, String fmt, int num) {
		return formatString(length, fmt, String.valueOf(num));
	}

	public static String formatString(int length, String fmt, String str) {
		if (fmt == null) {
			return str;
		}
		if (StringUtils.isBlank(str)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		if (str.length() >= length) {
			return str.substring(0, length);
		}
		int lenNum = length - str.length();
		for (int i = 0; i < lenNum; i++) {
			sb.append(fmt);
		}
		sb.append(str);
		return sb.toString();
	}

	public static boolean isMatcher(String str, String pattern) {
		boolean flag = false;
		if (isBlank(pattern) || isBlank(str)) {
			return false;
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		flag = m.lookingAt();
		return flag;
	}

	/**
	 * TODO list支持其他复杂对象
	 * 
	 * @param join
	 *            字符串连接符
	 * @param list
	 *            只支持基础数据类型对象
	 * @return
	 */
	public static String arrayToString(String join, List<?> list) {
		if (list == null || list.size() <= 0) {
			return "";
		}
		if (join == null) {
			join = "";
		}
		StringBuffer arraySb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i == list.size() - 1) {
				arraySb.append(list.get(i));
			} else {
				arraySb.append(list.get(i)).append(join);
			}
		}
		return arraySb.toString();
	}

	public static String toShowContent(String content, int length, String pattern) {
		String result = "";
		if (isEmpty(content)) {
			return "";
		}
		length = length <= 2 ? 15 : length;
		if (content.length() > length) {
			result = content.substring(0, length) + pattern;
		} else {
			return content;
		}
		return result;
	}
	
	public static String[] toArrayString(Collection<String> collection){
		if(collection == null){
			return null;
		}
		
		return collection.toArray(new String[collection.size()]);
	}
	
	public static String[] tokenizeToStringArray(String str, String delimiters){
		if(str == null){
			return null;
		}
		StringTokenizer st = new StringTokenizer(str,delimiters);
		List<String> tokens = new ArrayList<String>();
		if(st.hasMoreTokens()){
			String token = st.nextToken().trim();
			tokens.add(token);
		}
		return toArrayString(tokens);
	}
	
	public static String replace(String inString,String oldPattern,String newPattern){
		if(isEmpty(inString) || isEmpty(oldPattern) || newPattern == null){
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		int index = inString.indexOf(oldPattern);
		int patLen = oldPattern.length();
		while(index >= 0){
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = patLen + index;
			index = inString.indexOf(oldPattern, pos);
		}
		return sb.toString();
	} 
	
	public static String delete(String inString,String pattern){
		return replace(inString, pattern, "");
	}
	
	/**
	 * 
	 * 功能描述：分割的URL地址所带的参数
	 *
	 * @param splitStr 要分割的URL地址
	 * @return name=value对
	 *
	 * @author 谢彩莲
	 *
	 * @since 2015年4月23日
	 *
	 * @update:[变更日期YYYY-MM-DD][更改人姓名][变更描述]
	 */
	public static Map<String, String> splitUrlToParameMap(String url) {
		Map<String, String> parameMap = new ConcurrentHashMap<String, String>();
         if(StringUtils.isEmpty(url)) {
        	 return parameMap;
         }
    	 String[] urlAndParame = url.split("\\?");
		 if(urlAndParame.length > 1) {
			String[] parameNameValues = urlAndParame[1].split("&");
			System.out.println("length:" + parameNameValues.length);
			for(int i=0,len=parameNameValues.length; i < len; i++) {
				String[] parameNameValue = parameNameValues[i].split("=");
				String parameName = parameNameValue[0];
				String paramValue = parameNameValue.length > 1 ? parameNameValue[1] : "";
				parameMap.put(parameName, paramValue);
			}
		}
         return parameMap;
	}
	
	public static void main(String[] args) {
		String content = "<p>aaa<img src=\"/mcl-web/upload/image/cover/20150625/1435195570031059003.png\" title=\"1435195570031059003.png\" alt=\"blue.png\"/></p>";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("prodSec", content);
		jsonObject.put("prodName", "aaaa");
		jsonObject.put("tradeMake", "aaaa");
		jsonObject.put("test3", "aaaa");
		String url = "http://localhost:8080?data=" + jsonObject.toJSONString();
		System.out.println(JSONObject.toJSON(splitUrlToParameMap(url)));
	}
}
