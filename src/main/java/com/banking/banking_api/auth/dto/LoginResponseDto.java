package com.banking.banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload for user login")
public  record LoginResponseDto(

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQHRlc3QuY29tIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzExNzA0Nj")
        String token


){}

