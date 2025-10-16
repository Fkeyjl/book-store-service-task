package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CustomerDTO;
import com.epam.rd.autocode.spring.project.dto.UserRegistrationDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private CustomerDTO testCustomerDTO;
    private UserRegistrationDTO testRegistrationDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setRole(Role.CUSTOMER);
        testUser.setBalance(BigDecimal.valueOf(100.00));
        testUser.setPhone("+380123456789");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setIsBlocked(false);

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setEmail("test@example.com");
        testCustomerDTO.setName("Test User");

        testRegistrationDTO = new UserRegistrationDTO();
        testRegistrationDTO.setEmail("newuser@example.com");
        testRegistrationDTO.setPassword("password123");
        testRegistrationDTO.setName("New User");
    }

    @Test
    void testRegister_ShouldCreateNewUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(modelMapper.map(any(UserRegistrationDTO.class), eq(User.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.register(testRegistrationDTO);

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testFindUserById_WhenUserExists_ShouldReturnCustomerDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, CustomerDTO.class)).thenReturn(testCustomerDTO);

        CustomerDTO result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testUser, CustomerDTO.class);
    }

    @Test
    void testFindUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testFindUserByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.findUserByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testFindUserByEmail_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail("nonexistent@example.com"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testFindAllCustomers_ShouldReturnListOfCustomers() {
        User customer1 = new User();
        customer1.setId(1L);
        customer1.setRole(Role.CUSTOMER);

        User customer2 = new User();
        customer2.setId(2L);
        customer2.setRole(Role.CUSTOMER);

        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(Arrays.asList(customer1, customer2));
        when(modelMapper.map(any(User.class), eq(CustomerDTO.class))).thenReturn(new CustomerDTO());

        List<CustomerDTO> result = userService.findAllCustomers();

        assertThat(result).hasSize(2);
        verify(userRepository, times(1)).findByRole(Role.CUSTOMER);
        verify(modelMapper, times(2)).map(any(User.class), eq(CustomerDTO.class));
    }

    @Test
    void testAddUserBalance_ShouldIncreaseBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.addUserBalance(1L, BigDecimal.valueOf(50.00));

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(150.00));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testBlockUser_ShouldDisableUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.blockUser(1L);

        assertThat(testUser.getIsBlocked()).isTrue();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testUnblockUser_ShouldEnableUser() {
        testUser.setIsBlocked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.unblockUser(1L);

        assertThat(testUser.getIsBlocked()).isFalse();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }
}
