package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.Status;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {
    private Long orderId;
    private Status newStatus;
}
