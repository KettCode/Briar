package net.sf.briar.plugins.file;

import java.util.concurrent.Executor;

import net.sf.briar.api.android.AndroidExecutor;
import net.sf.briar.api.lifecycle.ShutdownManager;
import net.sf.briar.api.plugins.PluginExecutor;
import net.sf.briar.api.plugins.simplex.SimplexPlugin;
import net.sf.briar.api.plugins.simplex.SimplexPluginCallback;
import net.sf.briar.api.plugins.simplex.SimplexPluginFactory;
import net.sf.briar.api.protocol.TransportId;
import net.sf.briar.util.OsUtils;
import android.content.Context;

public class RemovableDrivePluginFactory implements SimplexPluginFactory {

	private static final long POLLING_INTERVAL = 10L * 1000L; // 10 seconds

	public TransportId getId() {
		return RemovableDrivePlugin.ID;
	}

	public SimplexPlugin createPlugin(@PluginExecutor Executor pluginExecutor,
			AndroidExecutor androidExecutor, Context appContext,
			ShutdownManager shutdownManager, SimplexPluginCallback callback) {
		RemovableDriveFinder finder;
		RemovableDriveMonitor monitor;
		if(OsUtils.isLinux()) {
			finder = new LinuxRemovableDriveFinder();
			monitor = new LinuxRemovableDriveMonitor();
		} else if(OsUtils.isMacLeopardOrNewer()) {
			finder = new MacRemovableDriveFinder();
			monitor = new MacRemovableDriveMonitor();
		} else if(OsUtils.isMac()) {
			// JNotify requires OS X 10.5 or newer, so we have to poll
			finder = new MacRemovableDriveFinder();
			monitor = new PollingRemovableDriveMonitor(pluginExecutor, finder,
					POLLING_INTERVAL);
		} else if(OsUtils.isWindows()) {
			finder = new WindowsRemovableDriveFinder();
			monitor = new PollingRemovableDriveMonitor(pluginExecutor, finder,
					POLLING_INTERVAL);
		} else {
			return null;
		}
		return new RemovableDrivePlugin(pluginExecutor, callback, finder,
				monitor);
	}
}
