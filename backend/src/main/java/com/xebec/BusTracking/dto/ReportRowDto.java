package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRowDto {
    private LocalDate date;
    private String routeName;
    private String busReg;
    private Long ticketsSold;
    private BigDecimal revenue;
}
