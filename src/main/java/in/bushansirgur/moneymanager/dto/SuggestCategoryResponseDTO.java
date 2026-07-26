package in.bushansirgur.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestCategoryResponseDTO {
    private Long categoryId;
    private String categoryName;
    private String icon;
    private String reason;
}
