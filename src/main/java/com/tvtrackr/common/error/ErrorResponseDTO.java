package com.tvtrackr.common.error;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
  private String code;
  private String desc;
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
}
