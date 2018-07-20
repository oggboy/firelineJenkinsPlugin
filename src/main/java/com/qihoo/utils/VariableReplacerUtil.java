package com.qihoo.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;

public class VariableReplacerUtil {

	/**
	 * Sets/exports shell env vars before original command, if the command contains variable name
	 * @param originalCommand
	 * @param vars
	 * @return shell script preluded with env vars
	 */
	public static String preludeWithEnvVars(String originalCommand, Map<String, String> vars,TaskListener listener) {
		if(originalCommand == null){
			return null;
		}
		if (vars == null) {
			return originalCommand;
		}

		String originalEnvVars = null;
		int beforeIndex = originalCommand.indexOf("${");
		int afterIndex = originalCommand.indexOf("}");
		if (afterIndex>beforeIndex){
			originalEnvVars = originalCommand.substring(beforeIndex+2,afterIndex);
		}else{
			return null;
		}

		vars.remove("_"); //why _ as key for build tool?
		for(Entry<String,String> entry:vars.entrySet()) {
			if (originalEnvVars.equalsIgnoreCase(entry.getKey()) ) {
				//listener.getLogger().println("key="+entry.getKey());
				//listener.getLogger().println("value="+entry.getValue());
				//listener.getLogger().println("originalEnvVars="+originalEnvVars);
				String envStr="${"+originalEnvVars+"}";
				originalCommand=originalCommand.replace(envStr,entry.getValue());
				break;
			}
		}
		//listener.getLogger().println("originalCommand="+originalCommand);
		return originalCommand;
	}
	
	public static String preludeWithBuild(Run<?, ?> run,TaskListener listener,String originalString) throws IOException, InterruptedException {
		Map<String, String> vars = new HashMap<String, String>();
		if(run!=null)
		{
			AbstractBuild<?, ?> build= (AbstractBuild<?, ?>) run;
			vars.putAll(build.getEnvironment(listener));
			vars.putAll(build.getBuildVariables());
		}
		return VariableReplacerUtil.preludeWithEnvVars(originalString, vars,listener);
	}


	/**
	 * @param run
	 * @param listener
	 * @param originalString
	 * @return 当发现使用环境变量时，自动去转换对应参数，否则返回原值
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static String checkEnvVars(Run<?, ?> run,TaskListener listener,String originalString) throws IOException, InterruptedException {
		if (originalString.contains("${")&&originalString.contains("}")){
			return preludeWithBuild(run,listener,originalString);
		}else{
			return originalString;
		}

	}
	/*public static String scrub(String command, Map<String, String> vars, Set<String> eyesOnlyVars) {
		if(command == null || vars == null || eyesOnlyVars == null){
			return command;
		}
		vars.remove("_");
		for (String sensitive : eyesOnlyVars) {
			for (String variable : vars.keySet()) {
				if (variable.equals(sensitive)) {
					String value = vars.get(variable);
					//TODO handle case sensitivity for command and each value
					if (command.contains(value)) {
						if (command.contains("\"" + value + "\"")) {
							command = command.replace(("\"" + value + "\"") , "**********");
						}
						command = command.replace(value , "**********");
					}
					break;
				}
			}
		}
		return command;
	}*/
}
