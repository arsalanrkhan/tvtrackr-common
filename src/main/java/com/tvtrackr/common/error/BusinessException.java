package com.tvtrackr.common.error;

import lombok.Getter;

public class BusinessException extends RuntimeException {

  @Getter private final BusinessErrors businessError;

  public BusinessException(Throwable cause, BusinessErrors businessError) {
    super(businessError.getDesc(), cause);
    this.businessError = businessError;
  }

  public BusinessException(BusinessErrors businessError) {
    super(businessError.getDesc());
    this.businessError = businessError;
  }
}
