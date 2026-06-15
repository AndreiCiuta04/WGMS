package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cleaning_schedule")
public class CleaningSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CleaningFrequency frequency;

    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, name = "fair_balancing_enabled")
    private boolean fairBalancingEnabled = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private ApplicationUser createdBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private ApplicationUser assignedTo;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (nextDueDate == null) {
            nextDueDate = startDate;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHouseholdId() {
        return household == null ? null : household.getId();
    }

    public String getCreatedByEmail() {
        return createdBy == null ? null : createdBy.getEmail();
    }

    public String getAssignedToEmail() {
        return assignedTo == null ? null : assignedTo.getEmail();
    }

    public boolean belongsToHousehold(Long householdId) {
        return household != null && Objects.equals(household.getId(), householdId);
    }

    public boolean isCreatedBy(ApplicationUser user) {
        return user != null
            && createdBy != null
            && Objects.equals(createdBy.getId(), user.getId());
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CleaningFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(CleaningFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFairBalancingEnabled() {
        return fairBalancingEnabled;
    }

    public void setFairBalancingEnabled(boolean fairBalancingEnabled) {
        this.fairBalancingEnabled = fairBalancingEnabled;
    }

    public ApplicationUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ApplicationUser createdBy) {
        this.createdBy = createdBy;
    }

    public ApplicationUser getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(ApplicationUser assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CleaningSchedule{"
            + "id=" + id
            + ", householdId=" + getHouseholdId()
            + ", title='" + title + '\''
            + ", frequency=" + frequency
            + ", startDate=" + startDate
            + ", endDate=" + endDate
            + ", nextDueDate=" + nextDueDate
            + ", active=" + active
            + ", fairBalancingEnabled=" + fairBalancingEnabled
            + ", createdByUserId=" + (createdBy == null ? null : createdBy.getId())
            + ", assignedToUserId=" + (assignedTo == null ? null : assignedTo.getId())
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }
}
