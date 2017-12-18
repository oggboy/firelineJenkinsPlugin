package com.qihoo.fireline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * 从jar包中取文件
 * yuanwei 
 * 2017-11-29
 */

public class JarCopy {
	/**
	* 从jar包中取文件
	* @param oldPath 文件路径	
	* @param newPath 文件取出路径
	*  
	*/
	public static void copyJarResource(String oldPath,String newPath) throws Exception {
		//创建目录
		File d = new File(newPath);
		File pathFile=new File(d.getParent());
		if (!pathFile.exists()) {
			try {
				if(!pathFile.mkdirs())
					System.out.println("Create Directory Fail,Please Make Sure Your Account Have READ And Write Permission!");
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Create Directory Fail,Please Make Sure Your Account Have READ And Write Permission!");
			}			
		}
		
		if (d.exists()) {
			if(!d.delete())
				System.out.println("Fail to delete file.");;
		}
		InputStream stream = null;
        OutputStream resStreamOut = null;

			try {
	            stream = JarCopy.class.getResourceAsStream(oldPath);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
	            
	            if(stream == null) {
	                throw new Exception("Cannot get resource \"" + oldPath + "\" from Jar file.");
	            }

	            int readBytes;
	            byte[] buffer = new byte[4096];
	            
	            resStreamOut = new FileOutputStream(d);
	            while ((readBytes = stream.read(buffer)) > 0) {
	                resStreamOut.write(buffer, 0, readBytes);
	            }
	        } catch (Exception ex) {
	            throw ex;
	        } finally {
	        	try {
	        		if (stream!=null) {
		        		 stream.close();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
	        	if (resStreamOut!=null) {
	        		resStreamOut.close();
				}
	            
	        }		
	}
	
	
	
        
        

}