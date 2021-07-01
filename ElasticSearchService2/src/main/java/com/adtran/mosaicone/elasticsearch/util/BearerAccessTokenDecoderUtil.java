package com.adtran.mosaicone.elasticsearch.util;

import java.util.Base64;

public class BearerAccessTokenDecoderUtil {
	
    public static Token decodeBearerAccessJWTToken(String bearAccessToken) {
       
    	Base64.Decoder bearAccessDecoder = Base64.getDecoder();
        String[] bearAccessTokenChunks = bearAccessToken.split("\\.");

        String tokenHeader = new String(bearAccessDecoder.decode(bearAccessTokenChunks[0]));
        String tokenPayload = new String(bearAccessDecoder.decode(bearAccessTokenChunks[1]));

        return new Token(tokenHeader, tokenPayload);
    }

}
