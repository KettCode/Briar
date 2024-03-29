package org.briarproject.briar.api.messaging;

import org.briarproject.bramble.api.FormatException;
import org.briarproject.bramble.api.client.ClientHelper;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.api.sync.ClientId;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.briar.api.conversation.ConversationManager.ConversationClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MessagingManager extends ConversationClient {

	/**
	 * The unique ID of the messaging client.
	 */
	ClientId CLIENT_ID = new ClientId("org.briarproject.briar.messaging");

	/**
	 * The current major version of the messaging client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the messaging client.
	 */
	int MINOR_VERSION = 2;

	/**
	 * Stores a local private message.
	 */
	void addLocalMessage(PrivateMessage m) throws DbException;

	/**
	 * Stores a local attachment message.
	 *
	 * @throws FileTooBigException If the attachment is too big
	 */
	AttachmentHeader addLocalAttachment(GroupId groupId, long timestamp,
			String contentType, InputStream is) throws DbException, IOException;

	/**
	 * Removes an unsent attachment.
	 */
	void removeAttachment(AttachmentHeader header) throws DbException;

	/**
	 * Returns the ID of the contact with the given private conversation.
	 */
	ContactId getContactId(GroupId g) throws DbException;

	/**
	 * Returns the ID of the private conversation with the given contact.
	 */
	GroupId getConversationId(ContactId c) throws DbException;

	/**
	 * Returns the text of the private message with the given ID, or null if
	 * the private message has no text.
	 */
	@Nullable
	String getMessageText(MessageId m) throws DbException;

	/**
	 * Returns the attachment with the given attachment header.
	 *
	 * @throws InvalidAttachmentException If the header refers to a message
	 * that is not an attachment, or to an attachment that does not have the
	 * expected content type
	 */
	Attachment getAttachment(AttachmentHeader h) throws DbException;

	/**
	 * Returns true if the contact with the given {@link ContactId} does support
	 * image attachments.
	 *
	 * Added: 2019-01-01
	 */

	void deleteMessageAuto(MessageId messageId) throws DbException;

	Collection<MessageId> GetMessageIdsToDelete() throws DbException;

	boolean IsMessageRead(MessageId messageId) throws DbException, FormatException;

	boolean contactSupportsImages(Transaction txn, ContactId c)
			throws DbException;

}
