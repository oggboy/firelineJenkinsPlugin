package com.qihoo.fireline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.qihoo.utils.FileUtils;
import com.qihoo.utils.StringUtils;
import com.qihoo.utils.VariableReplacerUtil;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.FormValidation;

public class FireLineTarget extends AbstractDescribableImpl<FireLineTarget> {
	private final boolean blockBuild;
	private final boolean csp;
	private final String configuration;
	private final String reportPath;
	private final String reportFileName;
	private final String jdk;
	private final String jvm;
	// notScan=true, not analyze code;notScan=false,analyze code.
	private final String buildWithParameter;

	@DataBoundConstructor
	public FireLineTarget(boolean csp,boolean blockBuild,String configuration, String reportPath, String reportFileName, String buildWithParameter, String jdk,String jvm) {
		this.csp=csp;
		this.blockBuild=blockBuild;
		this.configuration = StringUtils.trim(configuration);
		this.reportPath = StringUtils.trim(reportPath);
		this.reportFileName = StringUtils.trim(reportFileName);
		this.buildWithParameter = buildWithParameter;
		this.jdk = jdk;
		this.jvm=StringUtils.trim(jvm);
	}
	
	public boolean getCsp() {
		return this.csp;
	}
	
	public boolean getBlockBuild() {
		return this.blockBuild;
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

	public String getBuildWithParameter() {
		return this.buildWithParameter;
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
			if (value.length() > 0) {
				InputStream in=StringUtils.strToStream(value);
				if(in!=null && !FileUtils.checkXmlInputStream(in)) {
					return FormValidation.error("The XML configuration file format is illegal.");
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckReportPath(@QueryParameter String value) throws IOException, ServletException {

			if (value == null || value.length() == 0) {
				return FormValidation.error("Please input your report path");
			}
			if(!(value.contains("${")&&value.contains("}"))) {
				if (!FileUtils.existFile(value) || !(new File(value).isDirectory()))
					return FormValidation.error("The report path can't be found.");
			}
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
		public FormValidation doCheckBuildWithParameter(@QueryParameter String value) {
			if(value!=null&&value.length()>0) {
				if(!(value.substring(0, 2).equals("${")&&value.charAt(value.length()-1)=='}')){
					return FormValidation.error("The parameter value is illegal.");
				}
			}
			return FormValidation.ok();
		}

		public String defaultReportPath() {
			return FileUtils.defaultReportPath();
		}
	}
	
	/*public void handleAction(Run<?,?> build) {
//		System.out.println("--------------------handleAction-----------------");
		build.addAction(getProjectAction());
	}
	
	public Action getProjectAction() {
		return new FireLineScanCodeAction();
	}*/
}
