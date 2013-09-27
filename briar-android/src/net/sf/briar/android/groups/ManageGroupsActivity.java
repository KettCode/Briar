package net.sf.briar.android.groups;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static net.sf.briar.android.groups.ManageGroupsItem.NONE;
import static net.sf.briar.android.util.CommonLayoutParams.MATCH_MATCH;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.sf.briar.android.util.ListLoadingProgressBar;
import net.sf.briar.api.android.DatabaseUiExecutor;
import net.sf.briar.api.db.DatabaseComponent;
import net.sf.briar.api.db.DbException;
import net.sf.briar.api.db.event.DatabaseEvent;
import net.sf.briar.api.db.event.DatabaseListener;
import net.sf.briar.api.db.event.RemoteSubscriptionsUpdatedEvent;
import net.sf.briar.api.db.event.SubscriptionAddedEvent;
import net.sf.briar.api.db.event.SubscriptionRemovedEvent;
import net.sf.briar.api.lifecycle.LifecycleManager;
import net.sf.briar.api.messaging.Group;
import net.sf.briar.api.messaging.GroupStatus;
import roboguice.activity.RoboFragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.inject.Inject;

public class ManageGroupsActivity extends RoboFragmentActivity
implements DatabaseListener, OnItemClickListener {

	private static final Logger LOG =
			Logger.getLogger(ManageGroupsActivity.class.getName());

	private ManageGroupsAdapter adapter = null;
	private ListView list = null;
	private ListLoadingProgressBar loading = null;

	// Fields that are accessed from background threads must be volatile
	@Inject private volatile DatabaseComponent db;
	@Inject @DatabaseUiExecutor private volatile Executor dbUiExecutor;
	@Inject private volatile LifecycleManager lifecycleManager;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		adapter = new ManageGroupsAdapter(this);
		list = new ListView(this);
		list.setLayoutParams(MATCH_MATCH);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		// Show a progress bar while the list is loading
		loading = new ListLoadingProgressBar(this);
		setContentView(loading);
	}

	@Override
	public void onResume() {
		super.onResume();
		db.addListener(this);
		loadAvailableGroups();
	}

	private void loadAvailableGroups() {
		dbUiExecutor.execute(new Runnable() {
			public void run() {
				try {
					lifecycleManager.waitForDatabase();
					long now = System.currentTimeMillis();
					Collection<GroupStatus> available = db.getAvailableGroups();
					long duration = System.currentTimeMillis() - now;
					if(LOG.isLoggable(INFO))
						LOG.info("Load took " + duration + " ms");
					displayAvailableGroups(available);
				} catch(DbException e) {
					if(LOG.isLoggable(WARNING))
						LOG.log(WARNING, e.toString(), e);
				} catch(InterruptedException e) {
					if(LOG.isLoggable(INFO))
						LOG.info("Interrupted while waiting for database");
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	private void displayAvailableGroups(
			final Collection<GroupStatus> available) {
		runOnUiThread(new Runnable() {
			public void run() {
				setContentView(list);
				adapter.clear();
				for(GroupStatus g : available)
					adapter.add(new ManageGroupsItem(g));
				adapter.sort(ItemComparator.INSTANCE);
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		db.removeListener(this);
	}

	public void eventOccurred(DatabaseEvent e) {
		if(e instanceof RemoteSubscriptionsUpdatedEvent) {
			if(LOG.isLoggable(INFO))
				LOG.info("Remote subscriptions changed, reloading");
			loadAvailableGroups();
		} else if(e instanceof SubscriptionAddedEvent) {
			if(LOG.isLoggable(INFO)) LOG.info("Group added, reloading");
			loadAvailableGroups();
		} else if(e instanceof SubscriptionRemovedEvent) {
			if(LOG.isLoggable(INFO)) LOG.info("Group removed, reloading");
			loadAvailableGroups();
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ManageGroupsItem item = adapter.getItem(position);
		if(item == NONE) return;
		GroupStatus s = item.getGroupStatus();
		Group g = s.getGroup();
		Intent i = new Intent(this, ConfigureGroupActivity.class);
		i.putExtra("net.sf.briar.GROUP_ID", g.getId().getBytes());
		i.putExtra("net.sf.briar.GROUP_NAME", g.getName());
		i.putExtra("net.sf.briar.SUBSCRIBED", s.isSubscribed());
		i.putExtra("net.sf.briar.VISIBLE_TO_ALL", s.isVisibleToAll());
		startActivity(i);
	}

	private static class ItemComparator
	implements Comparator<ManageGroupsItem> {

		private static final ItemComparator INSTANCE = new ItemComparator();

		public int compare(ManageGroupsItem a, ManageGroupsItem b) {
			if(a == b) return 0;
			if(a == NONE) return 1;
			if(b == NONE) return -1;
			String aName = a.getGroupStatus().getGroup().getName();
			String bName = b.getGroupStatus().getGroup().getName();
			return String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
		}
	}
}