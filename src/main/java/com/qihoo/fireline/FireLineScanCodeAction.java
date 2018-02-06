package com.qihoo.fireline;

import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Action;
import hudson.model.ProminentProjectAction;
import jenkins.model.Jenkins;

public class FireLineScanCodeAction implements ProminentProjectAction {

	@Override
	public String getUrlName() {
		return null;
	}

	@Override
	public String getIconFileName() {
		PluginWrapper wrapper = null;
		Jenkins jenkins=Jenkins.getInstance();
		PluginManager pluginManager=jenkins.getPluginManager();
		if ( jenkins!= null && pluginManager!= null)
			wrapper = pluginManager.getPlugin("fireline");
		if (wrapper != null)
			return "/plugin/" + wrapper.getShortName() + "/images/fireLine_48x48.png";
		return null;
	}

	@Override
	public String getDisplayName() {
		return "FireLine Static Analysis";
	}
}
