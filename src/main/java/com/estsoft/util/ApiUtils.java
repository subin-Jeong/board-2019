package com.estsoft.util;

/**
 * API 내에서 사용할 유틸 클래스
 * @author JSB
 */
public class ApiUtils {

	/**
	 * 소수점 자리수 확인
	 * @param number
	 * @return 소수점 자리 개수
	 */
	public static int getDecimalLength(double number) {
		
		String numberString = number + "";
		return numberString.length() - numberString.indexOf(".") - 1;
		
	}
	
	/**
	 * 문자열 타입 변수 널체크
	 * @param string
	 * @return boolean
	 */
	public static boolean isNotNullString(String string) {
		
		if(string != null && !string.equals("") && string.trim().length() > 0) {
			return true;
		}
		return false;
		
	}
	
	/**
	 * 정수 변환 가능한 문자열인지 확인
	 * @param string
	 * @return boolean
	 */
	public static boolean isStringInteger(String string) {
		
		try {
			Integer.parseInt(string);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
		
	}
}
