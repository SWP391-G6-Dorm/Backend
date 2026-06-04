package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * A physical block or floor within a Property.
 * Maps to table: block_floors
 *
 * Example: Block A – Floor 2, Block B – Floor 1
 */
@Entity
@Table(
    name = "block_floors",
    indexes = {
        @Index(name = "idx_blockfloor_property", columnList = "property_id")
    }
)
public class BlockFloor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @NotBlank
    @Size(max = 100)
    @Column(name = "block_name", nullable = false, length = 100)
    private String blockName;

    @NotNull
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @OneToMany(mappedBy = "blockFloor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    public BlockFloor() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}
