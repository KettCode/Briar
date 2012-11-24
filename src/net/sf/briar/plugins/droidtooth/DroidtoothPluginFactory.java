package net.sf.briar.plugins.droidtooth;

import java.util.concurrent.Executor;

import net.sf.briar.api.android.AndroidExecutor;
import net.sf.briar.api.lifecycle.ShutdownManager;
import net.sf.briar.api.plugins.PluginExecutor;
import net.sf.briar.api.plugins.duplex.DuplexPlugin;
import net.sf.briar.api.plugins.duplex.DuplexPluginCallback;
import net.sf.briar.api.plugins.duplex.DuplexPluginFactory;
import net.sf.briar.api.protocol.TransportId;
import android.content.Context;

public class DroidtoothPluginFactory implements DuplexPluginFactory {

	private static final long POLLING_INTERVAL = 3L * 60L * 1000L; // 3 mins

	public TransportId getId() {
		return DroidtoothPlugin.ID;
	}

	public DuplexPlugin createPlugin(@PluginExecutor Executor pluginExecutor,
			AndroidExecutor androidExecutor, Context appContext,
			ShutdownManager shutdownManager, DuplexPluginCallback callback) {
		return new DroidtoothPlugin(pluginExecutor, androidExecutor, appContext,
				callback, POLLING_INTERVAL);
	}
}
