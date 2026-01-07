package com.nabto.webrtc.util.org.webrtc;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to normalize SDP to handle inconsistencies between peers.
 *
 * This specifically handles cases where:
 * 1. A peer generates different ICE credentials for each m-line despite using BUNDLE
 * 2. H264 profile-level-id needs adjustment for compatibility
 * 3. Answer SDP needs to match the offer's BUNDLE group
 */
public class SdpNormalizer {
    private static final String TAG = "SdpNormalizer";

    private static final Pattern BUNDLE_GROUP_PATTERN = Pattern.compile("a=group:BUNDLE\\s+(.+)");
    private static final Pattern ICE_UFRAG_PATTERN = Pattern.compile("a=ice-ufrag:(.+)");
    private static final Pattern ICE_PWD_PATTERN = Pattern.compile("a=ice-pwd:(.+)");
    private static final Pattern M_LINE_PATTERN = Pattern.compile("^m=");

    /**
     * Extracts the BUNDLE group line from an SDP.
     *
     * @param sdp The SDP string
     * @return The BUNDLE group line, or null if not found
     */
    public static String extractBundleGroup(String sdp) {
        if (sdp == null) return null;
        String[] lines = sdp.split("\r\n|\n");
        for (String line : lines) {
            if (line.startsWith("a=group:BUNDLE")) {
                return line;
            }
        }
        return null;
    }

    /**
     * Extracts ICE credentials (ufrag and pwd) from the first m-line section of an SDP.
     *
     * @param sdp The SDP string
     * @return An array of [ufrag, pwd], or null if not found
     */
    public static String[] extractIceCredentials(String sdp) {
        if (sdp == null) return null;

        String ufrag = null;
        String pwd = null;
        String[] lines = sdp.split("\r\n|\n");

        for (String line : lines) {
            if (line.startsWith("a=ice-ufrag:")) {
                ufrag = line.substring("a=ice-ufrag:".length());
            } else if (line.startsWith("a=ice-pwd:")) {
                pwd = line.substring("a=ice-pwd:".length());
            }
            if (ufrag != null && pwd != null) {
                return new String[]{ufrag, pwd};
            }
        }
        return null;
    }

    /**
     * Fixes an answer SDP to:
     * 1. Use the BUNDLE group from the offer
     * 2. Use consistent ICE credentials (from the answer's first section) across all bundled sections
     *
     * @param answerSdp The answer SDP to fix
     * @param offerSdp The offer SDP to get the BUNDLE group from
     * @return The fixed answer SDP
     */
    public static String fixAnswerSdp(String answerSdp, String offerSdp) {
        if (answerSdp == null || answerSdp.isEmpty()) {
            return answerSdp;
        }

        String offerBundle = extractBundleGroup(offerSdp);
        if (offerBundle == null) {
            Log.d(TAG, "fixAnswerSdp: Could not extract offer bundle");
            return answerSdp;
        }

        String[] answerCreds = extractIceCredentials(answerSdp);
        if (answerCreds == null) {
            Log.d(TAG, "fixAnswerSdp: Could not extract answer credentials");
            return answerSdp;
        }

        String answerUfrag = answerCreds[0];
        String answerPwd = answerCreds[1];

        Log.d(TAG, "fixAnswerSdp: Using offer bundle='" + offerBundle + "', answer ufrag='" + answerUfrag + "'");

        String[] lines = answerSdp.split("\r\n|\n");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip empty lines at the end
            if (line.isEmpty() && i == lines.length - 1) {
                continue;
            }

            if (line.startsWith("a=group:BUNDLE")) {
                // Replace answer's BUNDLE group with offer's
                result.append(offerBundle);
            } else if (line.startsWith("a=ice-ufrag:")) {
                // Use consistent ICE ufrag from answer's first section
                result.append("a=ice-ufrag:").append(answerUfrag);
            } else if (line.startsWith("a=ice-pwd:")) {
                // Use consistent ICE pwd from answer's first section
                result.append("a=ice-pwd:").append(answerPwd);
            } else {
                result.append(line);
            }
            result.append("\r\n");
        }

        return result.toString();
    }

    /**
     * Fixes an incoming offer SDP for compatibility:
     * 1. Fixes H264 profile-level-id from 42001f to 42e01f for better compatibility
     * 2. Fixes H264 codec name case from h264 to H264
     *
     * @param offerSdp The offer SDP to fix
     * @return The fixed offer SDP
     */
    public static String fixOfferSdp(String offerSdp) {
        if (offerSdp == null || offerSdp.isEmpty()) {
            return offerSdp;
        }

        String result = offerSdp;

        // Fix H264 profile: 42001f -> 42e01f for compatibility
        String beforeProfile = result;
        result = result.replace("profile-level-id=42001f", "profile-level-id=42e01f");
        if (!result.equals(beforeProfile)) {
            Log.d(TAG, "Fixed H264 profile: 42001f -> 42e01f");
        }

        // Fix H264 codec name case: h264 -> H264
        String beforeCase = result;
        result = result.replace(" h264/", " H264/");
        if (!result.equals(beforeCase)) {
            Log.d(TAG, "Fixed H264 codec name case: h264 -> H264");
        }

        return result;
    }

    /**
     * Normalizes the SDP to ensure all m-lines in a BUNDLE group share the same ICE credentials.
     *
     * When a remote peer incorrectly generates different ICE credentials for each m-line
     * despite using BUNDLE, this method fixes the SDP by using the ICE credentials from
     * the first m-line for all bundled m-lines.
     *
     * @param sdp The original SDP string
     * @return The normalized SDP with consistent ICE credentials
     */
    public static String normalizeIceCredentials(String sdp) {
        if (sdp == null || sdp.isEmpty()) {
            return sdp;
        }

        // Check if BUNDLE is being used
        Matcher bundleMatcher = BUNDLE_GROUP_PATTERN.matcher(sdp);
        if (!bundleMatcher.find()) {
            // No BUNDLE, nothing to normalize
            return sdp;
        }

        String[] lines = sdp.split("\r\n|\n");
        StringBuilder result = new StringBuilder();

        String firstIceUfrag = null;
        String firstIcePwd = null;
        boolean inFirstMLine = false;
        boolean foundFirstMLine = false;
        boolean modified = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip empty lines at the end
            if (line.isEmpty() && i == lines.length - 1) {
                continue;
            }

            // Detect m-line boundaries
            if (M_LINE_PATTERN.matcher(line).find()) {
                if (!foundFirstMLine) {
                    foundFirstMLine = true;
                    inFirstMLine = true;
                } else {
                    inFirstMLine = false;
                }
            }

            // Extract ICE credentials from first m-line section
            if (inFirstMLine) {
                Matcher ufragMatcher = ICE_UFRAG_PATTERN.matcher(line);
                if (ufragMatcher.matches()) {
                    firstIceUfrag = ufragMatcher.group(1);
                }
                Matcher pwdMatcher = ICE_PWD_PATTERN.matcher(line);
                if (pwdMatcher.matches()) {
                    firstIcePwd = pwdMatcher.group(1);
                }
            }

            // Replace ICE credentials in subsequent m-lines with first m-line's credentials
            if (!inFirstMLine && foundFirstMLine && firstIceUfrag != null && firstIcePwd != null) {
                Matcher ufragMatcher = ICE_UFRAG_PATTERN.matcher(line);
                if (ufragMatcher.matches()) {
                    String currentUfrag = ufragMatcher.group(1);
                    if (!currentUfrag.equals(firstIceUfrag)) {
                        line = "a=ice-ufrag:" + firstIceUfrag;
                        modified = true;
                        Log.d(TAG, "Normalized ice-ufrag from " + currentUfrag + " to " + firstIceUfrag);
                    }
                }
                Matcher pwdMatcher = ICE_PWD_PATTERN.matcher(line);
                if (pwdMatcher.matches()) {
                    String currentPwd = pwdMatcher.group(1);
                    if (!currentPwd.equals(firstIcePwd)) {
                        line = "a=ice-pwd:" + firstIcePwd;
                        modified = true;
                        Log.d(TAG, "Normalized ice-pwd");
                    }
                }
            }

            result.append(line);
            result.append("\r\n");
        }

        if (modified) {
            Log.i(TAG, "SDP was normalized to fix conflicting ICE credentials in BUNDLE");
        }

        return result.toString();
    }
}
