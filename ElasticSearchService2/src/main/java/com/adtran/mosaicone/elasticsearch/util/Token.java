package com.adtran.mosaicone.elasticsearch.util;

public class Token {
    
    private String header;
    private String payload;
    
    public Token(String header, String payload) {
        this.header = header;
        this.payload = payload;
    }

    public Token() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
