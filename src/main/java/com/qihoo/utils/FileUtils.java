package com.qihoo.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FileUtils {
	public static boolean existFile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static void deleteAllFilesOfDir(File path) {
		if (path == null) {
			return;
		}
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		File[] files = path.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				deleteAllFilesOfDir(files[i]);
			}
		}
		// path.delete();
	}

	public static boolean createDir(String directory) {

		File dir = new File(directory);
		try {
			if (dir.exists()) {
				// System.out.println("*************File is null "+dir);
				return false;
			} else {
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

	public static String defaultReportPath() {
		File report = new File(System.getProperty("java.io.tmpdir") + "/report");
		try {
			if (!report.exists()) {
				try {
					report.mkdir();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return report.getPath();
	}

	public static boolean checkSuffixOfFileName(String filename, String suffix) {
		if (filename == null || suffix == null)
			return false;
		if (!filename.endsWith("." + suffix))
			return false;
		if (filename.endsWith(".xml")) {
			try {
				DocumentBuilderFactory foctory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = foctory.newDocumentBuilder();
				builder.parse(new File(filename));
				return true;
			} catch(RuntimeException e) {
				return false;
			}catch (Exception e) {
				return false;
			}
		}
		return true;
	}

}
