package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * An image belonging to a Room.
 * Maps to table: room_images
 *
 * Business rules:
 *  - FR-PR-02: at least 3 images required before a room can be published
 *               (validated at service layer).
 *  - Only one image per room may have isPrimary = true.
 */
@Entity
@Table(
    name = "room_images",
    indexes = {
        @Index(name = "idx_roomimg_room",      columnList = "room_id"),
        @Index(name = "idx_roomimg_is_primary", columnList = "is_primary")
    }
)
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotBlank
    @Size(max = 500)
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /** Whether this is the cover/primary image shown in listing cards. */
    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    /** Display order within the room gallery (ascending). */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public RoomImage() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
