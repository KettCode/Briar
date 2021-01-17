package org.briarproject.bramble.custom;

import android.os.Handler;

import org.briarproject.bramble.api.db.DatabaseComponent;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.sync.MessageStatus;
import org.briarproject.bramble.db.DatabaseComponentImpl_Factory;

import java.util.List;


public class AutoDelete {

	private DatabaseComponent db;

	public AutoDelete(DatabaseComponent db)
	{
		this.db = db;
	}

	public void StartHanlder()
	{
		final Handler handler = new Handler();
		final int delay = 300000; // 1000 milliseconds == 1 second // 300000 = 5 min.

		handler.postDelayed(new Runnable() {
			public void run() {
				DoWork();
				handler.postDelayed(this, delay);
			}
		}, delay);
	}

	private void DoWork()
	{
		try {
			Transaction txn = db.startTransaction(false);
			List<MessageStatus> messageStates = db.getReadMessageStaten(txn);
			for (MessageStatus messageState : messageStates) {
				if(messageState.isSeen())
				{
					db.deleteMessage(txn, messageState.getMessageId());
				}
			}
		}
		catch (DbException e)
		{

		}
	}
}
