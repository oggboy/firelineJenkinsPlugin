package com.qihoo.utils;

public class StringUtils {
	public static String getSanitizedName(String str) {
        String safeName = str;
        safeName = safeName.replace(" ", "_");
        return safeName;
    }
	public static boolean checkInvalidChar(String value) {
		if(value!=null) {			
			if(value.indexOf("*")>-1||value.indexOf("#")>-1||value.indexOf("/")>-1||value.indexOf("&")>-1||value.indexOf(" ")>-1)
				return true;			
		}
		return false;
	}
	public static String trim(String str) {
		return str.trim();
	}
}
