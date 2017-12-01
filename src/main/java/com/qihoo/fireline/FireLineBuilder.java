package com.qihoo.fireline;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link FireLineBuilder} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #configuration}) to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author weihao
 */
public class FireLineBuilder extends Builder implements SimpleBuildStep {
	private final String configuration;
	private final String reportPath;
	private String output;
	private static String jarFile = "/lib/firelineJar.jar";
	public static String platform = System.getProperty("os.name");

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public FireLineBuilder(String config, String reportPath) {
		this.configuration = config;
		this.reportPath = reportPath;
	}

	/**
	 * We'll use this from the {@code config.jelly}.
	 */
	/**
	 * @return
	 */
	public String getConfiguration() {
		return configuration;
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException {
		String projectPath = workspace.getRemote();
		String jarPath=null;
		jarPath = getFireLineJar(listener);
		//jarPath=new File(FireLineBuilder.class.getResource(jarFile).getFile()).getAbsolutePath();
		// check params
		if (!getDescriptor().existFile(projectPath))
			listener.getLogger().println("您扫描的项目路径不正确。");
		if (!checkReportPath(reportPath)) {
			listener.getLogger().println("您的扫描结果报告路径不正确。");
		}

		String cmd = "java -jar " + jarPath + " -s=" + projectPath + " -r=" + reportPath;

//		 listener.getLogger().println("workspace.getRemote()=" + workspace.getRemote());
//		 listener.getLogger().println("jarPath= " +jarPath);
//		 listener.getLogger().println("cmd= " + cmd);
		File report = new File(reportPath);
		if (report.exists()) {
			deleteAllFilesOfDir(report);
		} else {
			try{
				report.mkdir();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (!getDescriptor().getIsSelected() && configuration != null) {
			File conf = new File(configuration);
			if (conf.exists() && !conf.isDirectory())
				cmd = cmd + " config=" + configuration;
			else
				listener.getLogger().println("配置文件未找到。");
		}
		if (jarPath != null && new File(jarPath).exists()) {
			// execute fireline
			exeCmd(cmd);
			listener.getLogger().println(output);
			listener.getLogger().println("FireLine report path: " + reportPath);
		} else
			listener.getLogger().println("fireline.jar does not exist!!");
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	private void deleteAllFilesOfDir(File path) {
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

	private void exeCmd(String commandStr) {
		BufferedReader br = null;
		try {
			Process p = Runtime.getRuntime().exec(commandStr);
			br = new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			output = sb.toString();
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getFireLineJar(TaskListener listener) {
		String oldPath = new File(FireLineBuilder.class.getResource(jarFile).getFile()).getAbsolutePath();
//		listener.getLogger().println("oldPath= " +oldPath);
		int index1=oldPath.indexOf("file:");
		int index2=oldPath.indexOf("fireline.jar");
		String newPath= oldPath.substring(index1+6, index2)+"firelineJar.jar";
//		listener.getLogger().println("newPath= " +newPath);
		try {
			JarCopy.copyJarResource(jarFile, newPath);
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return newPath;
	}

	private boolean checkReportPath(String path) {
		if (path != null && path.length() > 0) {
			File filePath = new File(path);
			if (filePath.exists() && filePath.isDirectory())
				return true;
		}
		return false;
	}

	/**
	 * Descriptor for {@link FireLineBuilder}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * {@code src/main/resources/hudson/plugins/firelineplugin/FireLineBuilder/*.jelly}
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an
				// extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use {@code transient}.
		 */
		private boolean isSelected;

		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'configuration'.
		 *
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */

		public FormValidation doCheckConfiguration(@QueryParameter String value) throws IOException, ServletException {
			if (value != null && value.length() > 0) {
				if (!existFile(value) || new File(value).isDirectory())
					return FormValidation.error("您输入的配置文件不存在。");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckReportPath(@QueryParameter String value) throws IOException, ServletException {

			if (value == null || value.length() == 0) {
				return FormValidation.error("报告路径不能为空。");
			}
			if (!existFile(value) || !(new File(value).isDirectory()))
				return FormValidation.error("您输入报告路径不正确。");
			return FormValidation.ok();
		}

		public boolean existFile(String filename) {
			File file = new File(filename);
			if (file.exists()) {
				return true;
			}
			return false;
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */

		public String getDisplayName() {
			return "Execute FireLine";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			isSelected = formData.getBoolean("isSelected");
			save();
			return super.configure(req, formData);
		}

		/**
		 * This method returns true if the global configuration says we should
		 * speak French.
		 *
		 * The method name is bit awkward because global.jelly calls this method
		 * to determine the initial state of the checkbox by the naming
		 * convention.
		 */
		public boolean getIsSelected() {
			return isSelected;
		}

		public String defaultReportPath() {
			File report=new File(System.getProperty("user.home") + "/report");
			try {
				if (!report.exists()) {
					try {
						
						report.mkdir();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return report.getPath();
		}
	}
}
