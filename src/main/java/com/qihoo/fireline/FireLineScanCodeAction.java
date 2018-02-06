package com.qihoo.fireline;

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
		if (Jenkins.getInstance() != null && Jenkins.getInstance().getPluginManager() != null)
			wrapper = Jenkins.getInstance().getPluginManager().getPlugin("fireline");
		if (wrapper != null)
			return "/plugin/" + wrapper.getShortName() + "/images/fireLine_48x48.png";
		return null;
	}

	@Override
	public String getDisplayName() {
		return "FireLine Static Analysis";
	}
}
