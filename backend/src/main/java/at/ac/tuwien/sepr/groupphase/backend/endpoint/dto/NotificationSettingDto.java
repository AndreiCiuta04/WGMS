package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class NotificationSettingDto {
    private boolean notifyTaskAssigned;
    private boolean notifyTaskOverdue;
    private boolean notifyExpenseCreated;
    private boolean notifyPaymentReceived;
    private boolean notifyCleaningReminder;
    private boolean channelInApp;
    private boolean channelEmail;
    private boolean channelPush;

    public boolean isNotifyTaskAssigned() {
        return notifyTaskAssigned;
    }

    public void setNotifyTaskAssigned(boolean notifyTaskAssigned) {
        this.notifyTaskAssigned = notifyTaskAssigned;
    }

    public boolean isNotifyTaskOverdue() {
        return notifyTaskOverdue;
    }

    public void setNotifyTaskOverdue(boolean notifyTaskOverdue) {
        this.notifyTaskOverdue = notifyTaskOverdue;
    }

    public boolean isNotifyExpenseCreated() {
        return notifyExpenseCreated;
    }

    public void setNotifyExpenseCreated(boolean notifyExpenseCreated) {
        this.notifyExpenseCreated = notifyExpenseCreated;
    }

    public boolean isNotifyPaymentReceived() {
        return notifyPaymentReceived;
    }

    public void setNotifyPaymentReceived(boolean notifyPaymentReceived) {
        this.notifyPaymentReceived = notifyPaymentReceived;
    }

    public boolean isNotifyCleaningReminder() {
        return notifyCleaningReminder;
    }

    public void setNotifyCleaningReminder(boolean notifyCleaningReminder) {
        this.notifyCleaningReminder = notifyCleaningReminder;
    }

    public boolean isChannelInApp() {
        return channelInApp;
    }

    public void setChannelInApp(boolean channelInApp) {
        this.channelInApp = channelInApp;
    }

    public boolean isChannelEmail() {
        return channelEmail;
    }

    public void setChannelEmail(boolean channelEmail) {
        this.channelEmail = channelEmail;
    }

    public boolean isChannelPush() {
        return channelPush;
    }

    public void setChannelPush(boolean channelPush) {
        this.channelPush = channelPush;
    }
}
