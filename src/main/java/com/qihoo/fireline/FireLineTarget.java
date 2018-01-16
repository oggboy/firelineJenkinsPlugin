package com.qihoo.fireline;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.qihoo.utils.FileUtils;
import com.qihoo.utils.StringUtils;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

public class FireLineTarget extends AbstractDescribableImpl<FireLineTarget>{
	private final String configuration;
	private final String reportPath;
	private final String reportFileName;
	private final String jdk;
	//notScan=true, not analyze code;notScan=false,analyze code.
	private final boolean notScan;
	
	@DataBoundConstructor
	public FireLineTarget(String configuration, String reportPath,String reportFileName,boolean notScan,String jdk) {
		this.configuration = StringUtils.trim(configuration);
		this.reportPath = StringUtils.trim(reportPath);
		this.reportFileName=StringUtils.trim(reportFileName);
		this.notScan=notScan;
		this.jdk=jdk;
	}
	
	public String getConfiguration() {
		return this.configuration;
	}
	public String getReportPath() {
		return this.reportPath;
	}
	public String getReportFileName() {
		return StringUtils.getSanitizedName(this.reportFileName);
	}
	public boolean getNotScan() {
		return this.notScan;
	}
	public String getJdk() {
		return jdk != null && !jdk.isEmpty() ? jdk : "(Inherit From Job)";
	}
	
	/*@DataBoundSetter
	public void setJdk(String jdk) {
		this.jdk = jdk;
	}*/
	
	@Extension
    public static class DescriptorImpl extends Descriptor<FireLineTarget> {
        public String getDisplayName() { 
        	return ""; 
        }
        
        public FormValidation doCheckConfiguration(@QueryParameter String value) throws IOException, ServletException {
			if (value != null && value.length() > 0) {
				if (!FileUtils.existFile(value) || new File(value).isDirectory())
					return FormValidation.error("The configuration file of FireLine doesn't exist.");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckReportPath(@QueryParameter String value) throws IOException, ServletException {

			if (value == null || value.length() == 0) {
				return FormValidation.error("Please set your report path");
			}
			if (!FileUtils.existFile(value) || !(new File(value).isDirectory()))
				return FormValidation.error("The report path can't be found.");
			return FormValidation.ok();
		}
		public FormValidation doCheckReportFileName(@QueryParameter String value) {
			if (value == null || value.length() == 0) {
				return FormValidation.error("Please set your report file name.");
			}			
			return FormValidation.ok();
		}
		public FormValidation doCheckJdk(@QueryParameter String value) {
			if (value.contains("1.8")||value.contains("1.7")) {
				return FormValidation.ok();
			}			
			return FormValidation.error("JDK1.7 or 1.8 is compatible with FireLine.");
		}                
        public String defaultReportPath() {
        	return FileUtils.defaultReportPath();
        }
    }
	
}
