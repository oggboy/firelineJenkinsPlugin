package com.qihoo.fireline;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.qihoo.utils.BuilderUtils;
import com.qihoo.utils.FileUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 * 
 * @author weihao
 */
public class FireLineBuilder extends Builder implements SimpleBuildStep {
	private final FireLineTarget fireLineTarget;
	private String jdk;
	// private String output;
	private static String jarFile = "/lib/firelineJar.jar";
	public final static String platform = System.getProperty("os.name");

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public FireLineBuilder(@CheckForNull FireLineTarget fireLineTarget) {
		// this.fireLineTargets=fireLineTargets != null ? new
		// ArrayList<FireLineTarget>(fireLineTargets) : new ArrayList<FireLineTarget>();
		this.fireLineTarget = fireLineTarget;
	}

	@CheckForNull
	public FireLineTarget getFireLineTarget() {
		return this.fireLineTarget;
	}
	
	@Override
	  public DescriptorImpl getDescriptor() {
	    return (DescriptorImpl) super.getDescriptor();
	  }

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		String jvmString = "-Xms1g -Xmx1g -XX:MaxPermSize=512m";
		EnvVars env = BuilderUtils.getEnvAndBuildVars(build, listener);
		String projectPath = workspace.getRemote();
		String reportFileNameTmp=fireLineTarget.getReportFileName().substring(0,fireLineTarget.getReportFileName().lastIndexOf("."));
		jdk = fireLineTarget.getJdk();
		String jarPath = null;
		//add actions 
//		build.addAction(new FireLineScanCodeAction());
		// Set JDK version
		computeJdkToUse(build, workspace, listener, env);
		//get path of fireline.jar
		jarPath = getFireLineJar(listener);
		// check params
		if (!FileUtils.existFile(projectPath))
			listener.getLogger().println("The path of project ：" + projectPath + "can't be found.");
		
		// 报告路径不存在时，创建该路径
		checkReportPath(fireLineTarget.getReportPath());
		String cmd = "java " + jvmString + " -jar " + jarPath + " -s=" + projectPath + " -r="
				+ fireLineTarget.getReportPath() + " reportFileName=" + reportFileNameTmp;

		if (fireLineTarget.getConfiguration() != null) {
			File conf = new File(fireLineTarget.getConfiguration());
			if (conf.exists() && !conf.isDirectory())
				cmd = cmd + " config=" + fireLineTarget.getConfiguration();
		}
//		listener.getLogger().println("----------Scan--------" + fireLineTarget.getNotScan());
		if (!fireLineTarget.getNotScan()) {
			// debug/	
//			if (checkFireLineJdk(getProject(build).getJDK())) {
				if (new File(jarPath).exists()) {
					// execute fireline
					listener.getLogger().println("FireLine start scanning...");
					exeCmd(cmd, listener);
					listener.getLogger().println("FireLine report path: " + fireLineTarget.getReportPath());
				} else {
					listener.getLogger().println("fireline.jar does not exist!!");
				}
			/*} else {
				listener.getLogger().println("The current JDK version is -------------"+getProject(build).getJDK().getHome());
				listener.getLogger().println("The current JDK version is not compatiable with FireLine, and jdk1.7 or jdk1.8 can be compatiable.");
			}*/
		}
	}

	private void exeCmd(String commandStr, TaskListener listener) {
		Process p = null;
		try {
			Runtime rt = Runtime.getRuntime();
			// listener.getLogger().println(commandStr);
			p = rt.exec(commandStr);
			listener.getLogger().println("CommandLine output:");
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", listener);
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "INFO", listener);
			errorGobbler.start();
			outputGobbler.start();
			p.waitFor();
		} catch (RuntimeException e) {
			throw (e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
	}

	private String getFireLineJar(TaskListener listener) {
		String oldPath = null;
		String newPath = null;
		if (platform.contains("Linux")) {
			oldPath = FireLineBuilder.class.getResource(jarFile).getFile();
			int index1 = oldPath.indexOf("file:");
			int index2 = oldPath.indexOf("fireline.jar");
			newPath = oldPath.substring(index1 + 5, index2) + "firelineJar.jar";
		} else {
			oldPath = new File(FireLineBuilder.class.getResource(jarFile).getFile()).getAbsolutePath();
			int index1 = oldPath.indexOf("file:");
			int index2 = oldPath.indexOf("fireline.jar");
			newPath = oldPath.substring(index1 + 6, index2) + "firelineJar.jar";
		}
		try {
			JarCopy.copyJarResource(jarFile, newPath);
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return newPath;
	}

	private void checkReportPath(String path) {
		if (path != null && path.length() > 0) {
			File filePath = new File(path);
			if (filePath.exists() && filePath.isDirectory()) {

			} else {
				try {
					filePath.mkdirs();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}

	public static String getMemUsage() {
		long free = java.lang.Runtime.getRuntime().freeMemory();
		long total = java.lang.Runtime.getRuntime().totalMemory();
		StringBuffer buf = new StringBuffer();
		buf.append("[Mem: used ").append((total - free) >> 20).append("M free ").append(free >> 20).append("M total ")
				.append(total >> 20).append("M]");
		return buf.toString();
	}

	private static AbstractProject<?, ?> getProject(Run<?, ?> run) {
		AbstractProject<?, ?> project = null;
		if (run instanceof AbstractBuild) {
			AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;
			project = build.getProject();
		}
		return project;
	}

	private void computeJdkToUse(Run<?, ?> build, FilePath workspace, TaskListener listener, EnvVars env)
			throws IOException, InterruptedException {
		JDK jdkToUse = getJdkToUse(getProject(build));
		if (jdkToUse != null) {
			Computer computer = workspace.toComputer();
			// just in case we are not in a build
			if (computer != null) {
				Node node=computer.getNode();
				if(node!=null)
					jdkToUse = jdkToUse.forNode(computer.getNode(), listener);
			}
			jdkToUse.buildEnvVars(env);
		}
	}

	/**
	 * @return JDK to be used with this project.
	 */
	private JDK getJdkToUse(@Nullable AbstractProject<?, ?> project) {
		JDK jdkToUse = getJdkFromJenkins();
		if (jdkToUse == null && project != null) {
			jdkToUse = project.getJDK();
		}
		return jdkToUse;
	}

	/**
	 * Gets the JDK that this builder is configured with, or null.
	 */
	@CheckForNull
	public JDK getJdkFromJenkins() {
		Jenkins jenkins=Jenkins.getInstance();
		if(jdk!=null&&jenkins!=null) {
			return jenkins.getJDK(jdk);
		}
		return null;
	}

	public boolean checkFireLineJdk(JDK jdkToUse) {
		String jdkPath=jdkToUse.getHome();
		return jdk != null && (jdkPath.contains("1.8") || jdkPath.contains("1.7"));
	}

	@Extension 
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
		@Override
		public String getDisplayName() {
			return "Execute FireLine";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			save();
			return super.configure(req, formData);
		}
	}
}
