package com.qihoo.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
	public static InputStream strToStream(String str) {
		if(str!=null && !str.trim().equals("")) {
			try {
				ByteArrayInputStream byteArrayIn=new ByteArrayInputStream(str.getBytes("UTF-8"));
				return byteArrayIn;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
