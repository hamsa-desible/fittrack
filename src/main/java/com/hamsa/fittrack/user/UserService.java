package com.hamsa.fittrack.user;

import com.hamsa.fittrack.activity.ActivityRepository;
import com.hamsa.fittrack.common.DuplicateResourceException;
import com.hamsa.fittrack.common.ResourceNotFoundException;
import com.hamsa.fittrack.user.UserDtos.CreateUserRequest;
import com.hamsa.fittrack.user.UserDtos.UserResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public UserService(UserRepository userRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user with email " + email + " already exists");
        }
        User saved = userRepository.save(new User(request.name().trim(), email, request.weightKg()));
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public void delete(Long id) {
        User user = getUser(id);
        activityRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }

    /** Used by other services that need the entity itself. */
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
