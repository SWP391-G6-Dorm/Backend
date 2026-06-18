package com.homestay.service;

import com.homestay.dto.request.UpdateMaintenanceStatusRequest;
import com.homestay.entity.*;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.MaintenanceTicketRepository;
import com.homestay.repository.RoomRepository;
import com.homestay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MaintenanceTicketService {

    private final MaintenanceTicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public MaintenanceTicketService(MaintenanceTicketRepository ticketRepository,
                                    BookingRepository bookingRepository,
                                    UserRepository userRepository,
                                    RoomRepository roomRepository) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    // ── Helper: Convert MaintenanceTicket entity → Map for frontend ──
    private Map<String, Object> ticketToMap(MaintenanceTicket ticket) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ticket.getId().toString());
        m.put("customerId", ticket.getCustomer().getId().toString());
        m.put("customerName", ticket.getCustomer().getFullName());
        m.put("customerEmail", ticket.getCustomer().getEmail());
        m.put("roomId", ticket.getRoom().getId().toString());
        m.put("roomNumber", ticket.getRoom().getRoomNumber());
        m.put("propertyId", ticket.getRoom().getProperty().getId().toString());
        m.put("propertyName", ticket.getRoom().getProperty().getName());
        m.put("title", ticket.getTitle());
        m.put("description", ticket.getDescription());

        // photoUrls: parse JSON string to array, or empty array
        List<String> photos = new ArrayList<>();
        if (ticket.getPhotoUrls() != null && !ticket.getPhotoUrls().isBlank()) {
            String raw = ticket.getPhotoUrls().trim();
            if (raw.startsWith("[")) {
                raw = raw.substring(1, raw.length() - 1);
                for (String s : raw.split(",")) {
                    s = s.trim().replaceAll("^\"|\"$", "");
                    if (!s.isEmpty()) photos.add(s);
                }
            } else {
                photos.add(raw);
            }
        }
        m.put("photoUrls", photos);

        m.put("status", ticket.getStatus().name());
        m.put("resolutionNote", ticket.getResolutionNote());
        m.put("createdAt", ticket.getCreatedAt().toString());
        m.put("updatedAt", ticket.getUpdatedAt().toString());
        return m;
    }

    // ── 1. Customer: Get paginated tickets with optional status filter ──
    public Map<String, Object> getCustomerTicketsPaged(UUID customerId, String status, int page, int size) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<MaintenanceTicket> allTickets = ticketRepository.findByCustomerOrderByCreatedAtDesc(customer);

        // Filter by status if not ALL
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            allTickets = allTickets.stream()
                    .filter(t -> t.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        int totalElements = allTickets.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> content = allTickets.subList(fromIndex, toIndex).stream()
                .map(this::ticketToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        return result;
    }

    // ── 2. Customer: Create ticket from multipart form ──
    public Map<String, Object> createTicketFromForm(UUID customerId, UUID roomId, String title, String description, List<MultipartFile> photos) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Find a matching booking for this customer + room
        List<Booking> bookings = bookingRepository.findByCustomerOrderByCreatedAtDesc(customer);
        Booking matchingBooking = bookings.stream()
                .filter(b -> b.getRoom().getId().equals(roomId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No booking found for this room. You can only submit maintenance requests for rooms you have booked."));

        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setCustomer(customer);
        ticket.setRoom(room);
        ticket.setBooking(matchingBooking);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(MaintenanceTicket.Status.OPEN);

        // Handle photo uploads (save file names as JSON array string)
        if (photos != null && !photos.isEmpty()) {
            List<String> photoUrls = new ArrayList<>();
            for (MultipartFile photo : photos) {
                // For simplicity, store original filename; in production, save to disk/S3
                photoUrls.add("/uploads/tickets/" + UUID.randomUUID() + "_" + photo.getOriginalFilename());
            }
            ticket.setPhotoUrls(photoUrls.toString());
        }

        MaintenanceTicket saved = ticketRepository.saveAndFlush(ticket);
        // Re-fetch to ensure @CreationTimestamp/@UpdateTimestamp are populated
        saved = ticketRepository.findById(saved.getId()).orElse(saved);
        return ticketToMap(saved);
    }

    // ── 3. Customer/Manager: Get ticket detail ──
    public Map<String, Object> getTicketDetail(User user, UUID ticketId) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (user.getRole() != User.Role.MANAGER && !ticket.getCustomer().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to view this ticket");
        }

        return ticketToMap(ticket);
    }

    // ── 4. Customer: Update ticket content (only OPEN) ──
    public Map<String, Object> updateTicketContent(UUID customerId, UUID ticketId, String title, String description) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("You do not have permission to update this ticket");
        }
        if (ticket.getStatus() != MaintenanceTicket.Status.OPEN) {
            throw new BusinessException("Cannot update ticket because it is already being processed");
        }

        if (title != null && !title.isBlank()) ticket.setTitle(title);
        if (description != null && !description.isBlank()) ticket.setDescription(description);

        MaintenanceTicket updated = ticketRepository.save(ticket);
        return ticketToMap(updated);
    }

    // ── 5. Customer: Delete ticket (only OPEN) ──
    public void deleteTicket(UUID customerId, UUID ticketId) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("You do not have permission to delete this ticket");
        }

        if (ticket.getStatus() != MaintenanceTicket.Status.OPEN) {
            throw new BusinessException("Cannot delete ticket because it is already being processed");
        }

        ticketRepository.delete(ticket);
    }

    // ── 6. Manager: Get all tickets (paginated, with status filter) ──
    public Map<String, Object> getAllTicketsPaged(String status, int page, int size) {
        List<MaintenanceTicket> allTickets = ticketRepository.findAllByOrderByCreatedAtDesc();

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            allTickets = allTickets.stream()
                    .filter(t -> t.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        int totalElements = allTickets.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> content = allTickets.subList(fromIndex, toIndex).stream()
                .map(this::ticketToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        return result;
    }

    // ── 7. Manager: Update ticket status ──
    public Map<String, Object> updateTicketStatusAndReturn(UUID ticketId, UpdateMaintenanceStatusRequest request) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setStatus(request.getStatus());
        if (request.getResolutionNote() != null) {
            ticket.setResolutionNote(request.getResolutionNote());
        }

        MaintenanceTicket updated = ticketRepository.save(ticket);
        return ticketToMap(updated);
    }

    // ── 8. Customer: Get active bookings for Create Ticket dropdown ──
    public List<Map<String, Object>> getActiveBookingsForCustomer(UUID customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Booking> bookings = bookingRepository.findByCustomerOrderByCreatedAtDesc(customer);

        // Only return CONFIRMED or CHECKED_IN bookings (active ones)
        return bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED || b.getStatus() == Booking.Status.CHECKED_IN)
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bookingId", b.getId().toString());
                    m.put("roomId", b.getRoom().getId().toString());
                    m.put("roomNumber", b.getRoom().getRoomNumber());
                    m.put("propertyId", b.getRoom().getProperty().getId().toString());
                    m.put("propertyName", b.getRoom().getProperty().getName());
                    m.put("checkInDate", b.getCheckInDate().toString());
                    m.put("checkOutDate", b.getCheckOutDate().toString());
                    m.put("status", b.getStatus().name());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
