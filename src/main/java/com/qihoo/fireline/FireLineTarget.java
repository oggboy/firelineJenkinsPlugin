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
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.FormValidation;

public class FireLineTarget extends AbstractDescribableImpl<FireLineTarget> {
	private final String configuration;
	private final String reportPath;
	private final String reportFileName;
	private final String jdk;
	private final String jvm;
	// notScan=true, not analyze code;notScan=false,analyze code.
	private final boolean notScan;

	@DataBoundConstructor
	public FireLineTarget(String configuration, String reportPath, String reportFileName, boolean notScan, String jdk,String jvm) {
		this.configuration = StringUtils.trim(configuration);
		this.reportPath = StringUtils.trim(reportPath);
		this.reportFileName = StringUtils.trim(reportFileName);
		this.notScan = notScan;
		this.jdk = jdk;
		this.jvm=StringUtils.trim(jvm);
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
	
	public String getJvm() {
		return jvm!=null&&!jvm.isEmpty()?jvm:"-Xms1g -Xmx1g -XX:MaxPermSize=512m";
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<FireLineTarget> {
		@Override
		public String getDisplayName() {
			return "";
		}

		public FormValidation doCheckConfiguration(@QueryParameter String value) throws IOException, ServletException {
			if (value != null && value.length() > 0) {
				if (!FileUtils.existFile(value) || new File(value).isDirectory()) {
					return FormValidation.error("The configuration file of FireLine doesn't exist.");
				}
				if (!FileUtils.checkSuffixOfFileName(value, "xml")) {
					return FormValidation.error("The XML configuration file format is illegal.");
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckReportPath(@QueryParameter String value) throws IOException, ServletException {

			if (value == null || value.length() == 0) {
				return FormValidation.error("Please input your report path");
			}
			if (!FileUtils.existFile(value) || !(new File(value).isDirectory()))
				return FormValidation.error("The report path can't be found.");
			return FormValidation.ok();
		}

		public FormValidation doCheckReportFileName(@QueryParameter String value) {
			if (value == null || value.length() == 0) {
				return FormValidation.error("Please input your report file name.");
			}
			if (!FileUtils.checkSuffixOfFileName(value, "html")) {
				return FormValidation.error("Please input the report file name with suffix of \"html\".");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckJdk(@QueryParameter String value) {
//			if (value.contains("1.8") || value.contains("1.7")) {
				return FormValidation.ok();
//			}
//			return FormValidation.error("JDK1.7 or 1.8 is compatible with FireLine.");
		}
		
		public FormValidation doCheckJvm(@QueryParameter String value) {
			if (value == null || value.length() == 0) {
				return FormValidation.error("Please input JVM parameter.");
			}
			return FormValidation.ok();
		}

		public String defaultReportPath() {
			return FileUtils.defaultReportPath();
		}
	}
	
	/*public void handleAction(Run<?,?> build) {
		build.addAction(new FireLineScanCodeAction(notScan));
	}
	
	public void getAction() {
		return new FireLineScanCodeAction();
	}
	public class FireLineScanCodeAction implements Action{
		private final boolean isScan;
		@DataBoundConstructor
		public FireLineScanCodeAction(boolean isScan) {
			this.isScan=isScan;
		}
		public FireLineScanCodeAction() {
			new FireLineScanCodeAction(isScan);
		}
		public boolean getIsScan() {
			return this.isScan;
		}
	    @Override
	    public String getUrlName() {
	        return "FireLine_Analysis_Code";
	    }
	    @Override
	    public String getIconFileName() {
	        return "fireLine.icon";
	    }

	    @Override
	    public String getDisplayName() {
	        return "FireLine Static Analysis";
	    }	    
	}*/

}
