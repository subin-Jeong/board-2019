package com.estsoft.util;

/**
 * API ������ ����� ��ƿ Ŭ����
 * @author JSB
 */
public class ApiUtils {

	/**
	 * �Ҽ��� �ڸ��� Ȯ��
	 * @param number
	 * @return �Ҽ��� �ڸ� ����
	 */
	public static int getDecimalLength(double number) {
		
		String numberString = number + "";
		return numberString.length() - numberString.indexOf(".") - 1;
		
	}
	
	/**
	 * ���ڿ� Ÿ�� ���� ��üũ
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
	 * ���� ��ȯ ������ ���ڿ����� Ȯ��
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
