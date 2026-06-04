package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * A rentable room belonging to a Property.
 * Maps to table: rooms
 */
@Entity
@Table(
    name = "rooms",
    indexes = {
        @Index(name = "idx_rooms_property", columnList = "property_id"),
        @Index(name = "idx_rooms_status", columnList = "status")
    }
)
public class Room {

    public enum GenderType { MALE, FEMALE, MIXED }
    public enum Status { AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE, DRAFT, ARCHIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /** Optional block/floor grouping within the property. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_floor_id")
    private BlockFloor blockFloor;

    @NotBlank
    @Size(max = 20)
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    /** Room category label, e.g. Studio, Single, Double, Dormitory. */
    @Size(max = 100)
    @Column(name = "room_type", length = 100)
    private String roomType;

    /** Legacy code field kept for backward compatibility. */
    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_type", nullable = false, length = 10)
    private GenderType genderType = GenderType.MIXED;

    @NotNull
    @Column(name = "price_per_month", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerMonth;

    /**
     * JSON array of amenity IDs, e.g. ["wifi","ac","parking"].
     * Stored as NVARCHAR(MAX) for SQL Server compatibility.
     */
    @Column(name = "amenities", columnDefinition = "NVARCHAR(MAX)")
    private String amenities;

    /**
     * JSON array of image CDN URLs (legacy fallback).
     * Prefer using RoomImage entities for new uploads.
     */
    @Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
    private String images;

    @Column(name = "floorplan_url", length = 500)
    private String floorplanUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalRequest> rentalRequests;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UtilityMeter> utilityMeters;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceTicket> maintenanceTickets;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> roomImages;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public Room() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public BlockFloor getBlockFloor() { return blockFloor; }
    public void setBlockFloor(BlockFloor blockFloor) { this.blockFloor = blockFloor; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public GenderType getGenderType() { return genderType; }
    public void setGenderType(GenderType genderType) { this.genderType = genderType; }

    public BigDecimal getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getFloorplanUrl() { return floorplanUrl; }
    public void setFloorplanUrl(String floorplanUrl) { this.floorplanUrl = floorplanUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<RentalRequest> getRentalRequests() { return rentalRequests; }
    public void setRentalRequests(List<RentalRequest> rentalRequests) { this.rentalRequests = rentalRequests; }

    public List<UtilityMeter> getUtilityMeters() { return utilityMeters; }
    public void setUtilityMeters(List<UtilityMeter> utilityMeters) { this.utilityMeters = utilityMeters; }

    public List<MaintenanceTicket> getMaintenanceTickets() { return maintenanceTickets; }
    public void setMaintenanceTickets(List<MaintenanceTicket> maintenanceTickets) { this.maintenanceTickets = maintenanceTickets; }

    public List<RoomImage> getRoomImages() { return roomImages; }
    public void setRoomImages(List<RoomImage> roomImages) { this.roomImages = roomImages; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
