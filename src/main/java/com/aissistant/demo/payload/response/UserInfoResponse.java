package com.aissistant.demo.payload.response;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private String jwtCookie;

    public String getJwtCookie() {
        return jwtCookie;
    }

    public void setJwtCookie(String jwtCookie) {
        this.jwtCookie = jwtCookie;
    }
}
