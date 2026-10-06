package com.arena.common;

import org.springframework.http.HttpStatus;

public class InvalidCustomerIdException extends ApiException {

  public InvalidCustomerIdException() {
    super(
        HttpStatus.BAD_REQUEST,
        "INVALID_CUSTOMER_ID",
        "Send your customer id as a UUID in the " + CustomerIds.HEADER + " header.");
  }
}
