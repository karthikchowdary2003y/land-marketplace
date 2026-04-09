package com.landmarket.service;

import com.landmarket.model.User;
import com.landmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateProfile(String email, String fullName, String address,
                               String city, String state, String phone) {
        User user = getUserByEmail(email);
        if (fullName != null) user.setFullName(fullName);
        if (address != null) user.setAddress(address);
        if (city != null) user.setCity(city);
        if (state != null) user.setState(state);
        if (phone != null && !phone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(phone)) {
                throw new RuntimeException("Phone number already in use");
            }
            user.setPhone(phone);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(String email) {
        User user = getUserByEmail(email);
        user.setActive(false);
        userRepository.save(user);
    }
}
