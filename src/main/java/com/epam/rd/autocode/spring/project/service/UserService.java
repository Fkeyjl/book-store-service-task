package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.UserRegistrationDTO;
import com.epam.rd.autocode.spring.project.dto.CustomerDTO;
import com.epam.rd.autocode.spring.project.dto.UserUpdateDTO;
import com.epam.rd.autocode.spring.project.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {
    void register(UserRegistrationDTO userRegistrationDTO);
    CustomerDTO findUserById(Long id);
    User findUserByEmail(String email);
    List<CustomerDTO> findAllCustomers();
    void updateUserProfile(UserUpdateDTO dto);
    User updateUserBalance(Long userId, BigDecimal amount);
    void deleteUser(Long userId);
    void blockUser(Long userId);
    void unblockUser(Long userId);
}
