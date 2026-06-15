package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_setting")
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private ApplicationUser user;

    @Column(name = "notify_task_assigned", nullable = false)
    private boolean notifyTaskAssigned = true;

    @Column(name = "notify_task_overdue", nullable = false)
    private boolean notifyTaskOverdue = true;

    @Column(name = "notify_expense_created", nullable = false)
    private boolean notifyExpenseCreated = true;

    @Column(name = "notify_payment_received", nullable = false)
    private boolean notifyPaymentReceived = true;

    @Column(name = "notify_cleaning_reminder", nullable = false)
    private boolean notifyCleaningReminder = true;

    @Column(name = "channel_in_app", nullable = false)
    private boolean channelInApp = true;

    @Column(name = "channel_email", nullable = false)
    private boolean channelEmail = true;

    @Column(name = "channel_push", nullable = false)
    private boolean channelPush = true;

    public NotificationSetting() {
    }

    public NotificationSetting(ApplicationUser user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

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
