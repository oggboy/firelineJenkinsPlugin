package com.qihoo.fireline;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.w3c.dom.Document;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qihoo.utils.BuilderUtils;
import com.qihoo.utils.FileUtils;
import com.qihoo.utils.VariableReplacerUtil;


import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample {@link Builder}.
 * 
 * @author weihao
 */
public class FireLineBuilder extends Builder implements SimpleBuildStep {
	private final FireLineTarget fireLineTarget;
	private String config;
	private String reportPath;
	private String jdk;
	private static String jarFile = "/lib/firelineJar.jar";
	public final static String platform = System.getProperty("os.name");
	private FireLineScanCodeAction fireLineAction=new FireLineScanCodeAction(); 

	@DataBoundConstructor
	public FireLineBuilder(@Nonnull FireLineTarget fireLineTarget) {
		this.fireLineTarget = fireLineTarget;
	}

	@Nonnull
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
		if(fireLineTarget==null) {
			listener.getLogger().println("fireLineTarget is null");
		}else {
			if(fireLineTarget.getCsp()) {
				initEnv();
			}
		
		EnvVars env = BuilderUtils.getEnvAndBuildVars(build, listener);
		String projectPath = workspace.getRemote();
		String reportFileNameTmp = fireLineTarget.getReportFileName().substring(0,
				fireLineTarget.getReportFileName().lastIndexOf("."));
		String jarPath = null;
        String cmd = null;
		String buildWithParameter = fireLineTarget.getBuildWithParameter();
		buildWithParameter = VariableReplacerUtil.checkEnvVars(build,listener,buildWithParameter);
		reportFileNameTmp = VariableReplacerUtil.checkEnvVars(build, listener, reportFileNameTmp);
		config=fireLineTarget.getConfiguration();
		reportPath=VariableReplacerUtil.checkEnvVars(build, listener, fireLineTarget.getReportPath());
		//listener.getLogger().println("reportPath="+reportPath);
		boolean IsExist=FileUtils.createDir(reportPath);
		if (!IsExist){
            listener.getLogger().println("结果报告路径创建失败，请确认当前Jenkins用户的权限");
        }
        if(fireLineTarget.getCsp()) {
			listener.getLogger().println("CSP="+System.getProperty("hudson.model.DirectoryBrowserSupport.CSP"));
		}
		jdk = fireLineTarget.getJdk();
		// add actions
        if(null!=fireLineAction){
            build.addAction(fireLineAction);
        }
		// Set JDK version
		computeJdkToUse(build, workspace, listener, env);
		// get path of fireline.jar
		jarPath = getFireLineJar(listener);
		// check params
		if (!FileUtils.existFile(projectPath)){
			listener.getLogger().println("The path of project ：" + projectPath + "can't be found.");
		}

		if (fireLineTarget.getJvm()!=null){
            cmd = "java " + fireLineTarget.getJvm() + " -jar " + jarPath + " -s=" + projectPath + " -r="
                    + reportPath + " reportFileName=" + reportFileNameTmp;
        }else{
            cmd = "java " + " -jar " + jarPath + " -s=" + projectPath + " -r="
                    + reportPath + " reportFileName=" + reportFileNameTmp;
        }
		if (config != null) {
			File confFile = new File(reportPath+File.separator+"config.xml");
            FileUtils.createXml(confFile,config);
			if (confFile.exists() && !confFile.isDirectory()){
                cmd = cmd + " config=" + confFile;
            }
		}
		if (buildWithParameter != null && buildWithParameter.contains("false")) {
			listener.getLogger().println("Build without FireLine !!!");
		} else {
			// debug/
			// if (checkFireLineJdk(getProject(build).getJDK())) {
			if (new File(jarPath).exists()) {
				// execute fireline
				listener.getLogger().println("FireLine start scanning...");
				//listener.getLogger().println("FireLine command="+cmd);
				exeCmd(cmd, listener);
				// if block number of report is not 0,then this build is set Failure.
				if(fireLineTarget.getBlockBuild()) {
					if (getBlockNum(reportPath, reportFileNameTmp) != 0) {
						
						build.setResult(Result.FAILURE);
						listener.getLogger().println(
								"[ERROR] There are some defects of \"Block\" level and FireLine set build result to FAILURE");
					}
				}
				listener.getLogger().println("FireLine report path: " + reportPath);
			} else {
				listener.getLogger().println("fireline.jar does not exist!!");
			}
		}
		}
	}
	
	private void initEnv() {
		System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-scripts; default-src *; style-src * http://* 'unsafe-inline' 'unsafe-eval'; script-src 'self' http://* 'unsafe-inline' 'unsafe-eval'");
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

	private int getBlockNum(String reportPath, String reportFileName) {
		String xmlPath = null;
		DocumentBuilderFactory foctory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		if (reportFileName != null && reportPath != null) {
			xmlPath = reportPath + java.io.File.separator + reportFileName + ".xml";
			try {
				builder = foctory.newDocumentBuilder();
				Document doc = builder.parse(new File(xmlPath));
				NodeList nodeLists = doc.getElementsByTagName("blocknum");
				if (nodeLists != null && nodeLists.getLength() > 0) {
					org.w3c.dom.Node node = nodeLists.item(0);
					if (node != null){
                        return Integer.parseInt(node.getTextContent());
                    }
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
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
				Node node = computer.getNode();
				if (node != null){
                    jdkToUse = jdkToUse.forNode(computer.getNode(), listener);
                }
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
		Jenkins jenkins = Jenkins.getInstance();
		if (jdk != null && jenkins != null) {
			return jenkins.getJDK(jdk);
		}
		return null;
	}

	public boolean checkFireLineJdk(JDK jdkToUse) {
		String jdkPath = jdkToUse.getHome();
		return jdk != null && (jdkPath.contains("1.8") || jdkPath.contains("1.7"));
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project){
		return fireLineAction;
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
