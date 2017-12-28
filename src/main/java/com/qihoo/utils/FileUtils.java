package com.qihoo.utils;

import java.io.File;

public class FileUtils {
	public static boolean existFile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	
	public static void deleteAllFilesOfDir(File path) {
		if (path==null) {
			return;
		}
		try{
			if (!path.exists())
				return;
			if (path.isFile()) {
				try {
					path.delete();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		File[] files = path.listFiles();
		if (files!=null) {
			for (int i = 0; i < files.length; i++) {
				deleteAllFilesOfDir(files[i]);
			}
		}
		// path.delete();
	}
	
	public static boolean createDir(String directory) {
		
		File dir=new File(directory);
		try {
			if (dir==null||dir.exists()) {
//				System.out.println("*************File is null  "+dir);
				return false;
			}else{
				try {
					return dir.mkdir();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

}
