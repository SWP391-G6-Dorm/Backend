package com.homestay.service;

import com.homestay.dto.request.CreateReviewRequest;
import com.homestay.dto.request.UpdateReviewRequest;
import com.homestay.dto.response.MyReviewResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Booking;
import com.homestay.entity.Review;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ForbiddenException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public MyReviewResponse submitReview(CreateReviewRequest request, User currentUser) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt phòng không tồn tại"));

        if (!booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không thể đánh giá đơn đặt phòng của người khác");
        }

        if (booking.getStatus() != Booking.Status.CHECKED_OUT) {
            throw new BusinessException("Chỉ có thể đánh giá sau khi đã checked-out");
        }

        if (reviewRepository.existsByBooking_Id(request.getBookingId())) {
            throw new BusinessException("Đơn đặt phòng này đã được đánh giá trước đó.");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(currentUser);
        review.setRoom(booking.getRoom());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(Review.Status.PUBLISHED);

        review = reviewRepository.save(review);
        return MyReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyReviewResponse> getMyReviews(User currentUser, Pageable pageable) {
        Page<Review> result = reviewRepository.findByCustomer_IdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return new PageResponse<>(
                result.getContent().stream().map(MyReviewResponse::fromEntity).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public MyReviewResponse updateReview(UUID id, UpdateReviewRequest request, User currentUser) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        if (!review.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa đánh giá này");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        return MyReviewResponse.fromEntity(review);
    }

    @Transactional
    public void deleteReview(UUID id, User currentUser) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        if (!review.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }
}
