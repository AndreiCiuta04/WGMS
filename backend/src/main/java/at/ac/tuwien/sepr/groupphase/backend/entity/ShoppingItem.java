package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single shopping-list entry. Scoped to a {@link Household}. FR-08 adds
 * {@link ShoppingItemVisibility} and an optional {@code targetUser} for direct
 * shopping requests like "Anna, please grab milk while you're at the store".
 */
@Entity
@Table(name = "shopping_item")
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Structured quantity: how many of {@link #quantityAmount} {@link #quantityUnit} (e.g. 5 × 3 L). All optional.
     */
    @Column(name = "quantity_count")
    private Integer quantityCount;

    @Column(name = "quantity_amount", precision = 10, scale = 3)
    private BigDecimal quantityAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_unit", length = 20)
    private QuantityUnit quantityUnit;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShoppingItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShoppingItemVisibility visibility;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Household household;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ApplicationUser createdBy;

    /**
     * Set only when {@link #visibility} is {@link ShoppingItemVisibility#TARGETED}.
     * Must be an active member of the same {@link #household}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser targetUser;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = ShoppingItemStatus.NEEDED;
        }
        if (visibility == null) {
            visibility = ShoppingItemVisibility.PERSONAL;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantityCount() {
        return quantityCount;
    }

    public void setQuantityCount(Integer quantityCount) {
        this.quantityCount = quantityCount;
    }

    public BigDecimal getQuantityAmount() {
        return quantityAmount;
    }

    public void setQuantityAmount(BigDecimal quantityAmount) {
        this.quantityAmount = quantityAmount;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ShoppingItemStatus getStatus() {
        return status;
    }

    public void setStatus(ShoppingItemStatus status) {
        this.status = status;
    }

    public ShoppingItemVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ShoppingItemVisibility visibility) {
        this.visibility = visibility;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public ApplicationUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ApplicationUser createdBy) {
        this.createdBy = createdBy;
    }

    public ApplicationUser getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(ApplicationUser targetUser) {
        this.targetUser = targetUser;
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
}
