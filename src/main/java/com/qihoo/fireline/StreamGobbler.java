package com.qihoo.fireline;

import hudson.model.TaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {  
    InputStream is;  
    String      type;  
    TaskListener listener;
  
    StreamGobbler(InputStream is, String type, TaskListener listener) {  
        this.is = is;  
        this.type = type;  
        this.listener=listener;
    }  
  
    public void run() {  
    	BufferedReader br = null;
        try {  
            InputStreamReader isr = new InputStreamReader(is,"UTF-8");  
            br = new BufferedReader(isr);  
            String line = null;  
            long start1=System.currentTimeMillis();
            while ((line = br.readLine()) != null){
            	listener.getLogger().println(type + ">" + line);  
            	listener.getLogger().println("需要 "+(System.currentTimeMillis()-start1)+"毫秒");
            }  
        } catch (IOException ioe) {  
            ioe.printStackTrace();  
        }  finally {
        	if(br!=null){
        		try {
					br.close();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
        	}
        }
    }  
}  
