package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be valid (10-15 digits)")
    private String phone;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    public static UserUpdateDTO fromCustomerDTO(CustomerDTO customerDTO) {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setId(customerDTO.getId());
        userUpdateDTO.setName(customerDTO.getName());
        userUpdateDTO.setEmail(customerDTO.getEmail());
        userUpdateDTO.setPhone(customerDTO.getPhone());
        userUpdateDTO.setBirthDate(customerDTO.getBirthDate());
        return userUpdateDTO;
    }
}
