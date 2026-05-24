package com.tvtrackr.common.error;

import lombok.Getter;

@Getter
public class BusinessErrors {

  public static final BusinessErrors GENERIC =
      new BusinessErrors("0000", "Generic Exception!", 500);

  protected String code;
  protected String desc;
  protected int httpStatus;
  protected String prefix;

  protected BusinessErrors() {}

  protected BusinessErrors(String code, String desc, int httpStatus) {
    this.prefix = "BE";
    this.code = prefix + code;
    this.desc = desc;
    this.httpStatus = httpStatus;
  }
}
