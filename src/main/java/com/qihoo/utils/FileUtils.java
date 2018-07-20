package com.qihoo.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

	public static boolean createDir(String destDirName) {
		if (destDirName == null){
			return false;
		}
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("目标目录已经存在");
            return true;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
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
		return true;
	}
	
	public static boolean checkXmlInputStream(InputStream in) {
		try {
			DocumentBuilderFactory foctory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = foctory.newDocumentBuilder();
			builder.parse(in);
			return true;
		} catch(RuntimeException e) {
			return false;
		}catch (Exception e) {
			return false;
		}
	}

	public static void createXml(File file, String value) {
		InputStream in=StringUtils.strToStream(value);
		TransformerFactory tf = TransformerFactory.newInstance();
		PrintWriter pw = null;
		try {
			DocumentBuilderFactory foctory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = foctory.newDocumentBuilder();
			Document doc = builder.parse(in);
			Transformer transformer = tf.newTransformer();
			DOMSource source = new DOMSource(doc);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"utf-8"));
			StreamResult result = new StreamResult(pw);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (TransformerException e) {
			System.out.println(e.getMessage());
		} catch (UnsupportedEncodingException e){
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			if (pw!=null) {
				pw.close();
			}
		}
	}

}
