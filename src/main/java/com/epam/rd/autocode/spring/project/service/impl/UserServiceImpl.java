package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.UserRegistrationDTO;
import com.epam.rd.autocode.spring.project.dto.CustomerDTO;
import com.epam.rd.autocode.spring.project.dto.UserUpdateDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(UserRegistrationDTO userRegistrationDTO) {
        if (userRepository.findByEmail(userRegistrationDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User is already exist");
        }
        User newUser = modelMapper.map(userRegistrationDTO, User.class);
        newUser.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
        userRepository.save(newUser);
    }

    @Override
    public CustomerDTO findUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, CustomerDTO.class))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<CustomerDTO> findAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER).stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void updateUserProfile(UserUpdateDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID  " + dto.getId() + " not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setBirthDate(dto.getBirthDate());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserBalance(Long userId, BigDecimal amount) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        if (userToUpdate.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance to place the order");
        }
        userToUpdate.setBalance(userToUpdate.getBalance().subtract(amount));
        return userRepository.save(userToUpdate);
    }

    @Override
    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        user.setIsBlocked(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        user.setIsBlocked(false);
        userRepository.save(user);
    }
}
