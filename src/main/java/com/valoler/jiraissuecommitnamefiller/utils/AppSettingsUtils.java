package com.valoler.jiraissuecommitnamefiller.utils;

import com.valoler.jiraissuecommitnamefiller.config.AppSettingsState;
import org.apache.commons.lang3.ArrayUtils;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class AppSettingsUtils {

    private static final String SEPARATOR = ":";

    private AppSettingsUtils() {
    }

    /**
     * Encode using Base64 user credentials as char arrays
     *
     * @param credentials credentials as char arrays to encode
     * @return encoded user credentials
     */
    public static String encodeUserCredentials(char[]... credentials) {
        AppSettingsState.getInstance().setCredentialsCount((long) credentials.length);
        return encodeCredential(
                Arrays.stream(credentials)
                      .map(AppSettingsUtils::encodeCredential)
                      .collect(Collectors.joining(SEPARATOR))
                      .toCharArray()
        );
    }

    /**
     * Encode using Base64 user credentials as char arrays
     *
     * @param credentials credentials as char arrays to encode
     * @return encoded user credentials
     */
    public static String encodeUserCredentials(List<char[]> credentials) {
        AppSettingsState.getInstance().setCredentialsCount((long) credentials.size());
        return encodeCredential(
                credentials
                        .stream()
                        .map(AppSettingsUtils::encodeCredential)
                        .collect(Collectors.joining(SEPARATOR))
                        .toCharArray()
        );
    }

    /**
     * Decodes user credentials encoded string and split for 3 encoded credentials fields
     *
     * @param encodedUserCredentials encoded user credentials
     * @return tuple 3 with encoded user credentials
     */
    public static String[] decodeUserCredentials(String encodedUserCredentials) {
        return decodeString(encodedUserCredentials).split(SEPARATOR);
    }

    /**
     * Encodes using Base64 input string
     *
     * @param credentialToEncode input credential as char array to encode
     * @return Base64 encoded string
     */
    public static String encodeCredential(char[] credentialToEncode) {
        return Base64.getEncoder()
                     .encodeToString(convertCharsToBytes(credentialToEncode));
    }

    /**
     * Decodes string from Base64 encoded
     *
     * @param encodedString Base64 encoded input string
     * @return decoded string
     */
    public static String decodeString(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString), StandardCharsets.UTF_8);
    }

    /**
     * Convert char array to byte array
     *
     * @param chars input char array
     * @return bytes array
     */
    private static byte[] convertCharsToBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public static String getBasicAuthenticationHeader(char[] login, char[] password) {
        return "Basic " + Base64.getEncoder()
                                .encodeToString(ArrayUtils.addAll(
                                        ArrayUtils.addAll(
                                                convertCharsToBytes(login),
                                                convertCharsToBytes(SEPARATOR.toCharArray())
                                        ),
                                        convertCharsToBytes(password)
                                ));
    }
}
