package com.example.biblov1.repository;

import com.example.biblov1.model.UserSwipe;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserSwipe.SwipeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {
    Optional<UserSwipe> findBySwiperAndSwiped(User swiper, User swiped);
    Optional<UserSwipe> findBySwiper_IdAndSwiped_Id(Long swiperId, Long swipedId);

    List<UserSwipe> findBySwiper(User swiper);
    void deleteBySwiperOrSwiped(User swiper, User swiped);

    boolean existsBySwiper_IdAndSwiped_IdAndSwipeType(Long swiperId, Long swipedId, SwipeType swipeType);
}
