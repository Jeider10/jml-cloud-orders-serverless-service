//package com.cloud.jml.client;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CurrentJwtTokenProvider {
//
//    public String getCurrentToken() {
//        Authentication authentication = SecurityContextHolder
//                .getContext()
//                .getAuthentication();
//
//        if (authentication != null && authentication.getCredentials() instanceof String token) {
//            return token;
//        }
//
//        return null;
//    }
//}
