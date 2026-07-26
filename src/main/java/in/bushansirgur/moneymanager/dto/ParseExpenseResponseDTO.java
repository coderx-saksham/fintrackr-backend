package in.bushansirgur.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParseExpenseResponseDTO {
    private String name;
    private BigDecimal amount;
    private LocalDate date;
    private Long categoryId;
    private String icon;
}
