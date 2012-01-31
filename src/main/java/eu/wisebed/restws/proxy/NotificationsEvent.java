package eu.wisebed.restws.proxy;

public class NotificationsEvent {

	private final Iterable<String> notifications;

	public NotificationsEvent(final Iterable<String> notifications) {
		this.notifications = notifications;
	}

	public Iterable<String> getNotifications() {
		return notifications;
	}
}
