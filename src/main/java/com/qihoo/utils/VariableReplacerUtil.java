package com.qihoo.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	public static String preludeWithEnvVars(String originalCommand, Map<String, String> vars) {
		if(originalCommand == null){
			return null;
		}
		if (vars == null) {
			return originalCommand;
		}
		vars.remove("_"); //why _ as key for build tool?
		StringBuilder sb = new StringBuilder();
		for(Entry<String,String> entry:vars.entrySet()) {
			//TODO handle case sensitivity for command and each variable
			if (originalCommand.contains(entry.getKey()) ) {
				sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"\n");
			}
		}
		sb.append("\n");
		sb.append(originalCommand);
		return sb.toString();
	}
	
	public static String preludeWithBuild(Run<?, ?> run,TaskListener listener,String originalString) throws IOException, InterruptedException {
		Map<String, String> vars = new HashMap<String, String>();
		if(run!=null)
		{
			AbstractBuild<?, ?> build= (AbstractBuild<?, ?>) run;
			vars.putAll(build.getEnvironment(listener));
			vars.putAll(build.getBuildVariables());
		}
		return VariableReplacerUtil.preludeWithEnvVars(originalString, vars);
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
