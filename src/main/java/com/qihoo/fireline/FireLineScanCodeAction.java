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
		PluginWrapper wrapper = Jenkins.getInstance().getPluginManager().getPlugin("fireline");
		return "/plugin/" + wrapper.getShortName() + "/images/fireLine_48x48.png";
	}

	@Override
	public String getDisplayName() {
		return "FireLine Static Analysis";
	}
}
