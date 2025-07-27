package com.shuaibu.components;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class MachineLockUtil {

    private static final String LOCK_FILENAME = "machine.lock";

    public static boolean verifyOrStoreMachine() {
        try {
            String fingerprint = getMachineFingerprint();
            File jarDir = new File(MachineLockUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getParentFile();
            File lockFile = new File(jarDir, LOCK_FILENAME);

            if (lockFile.exists()) {
                String stored = Files.readString(lockFile.toPath()).trim();
                return stored.equals(fingerprint);
            } else {
                Files.writeString(lockFile.toPath(), fingerprint);
                lockFile.setReadOnly(); // optional
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getMachineFingerprint() {
        String mb = getWmicValue("baseboard", "serialnumber");
        String cpu = getWmicValue("cpu", "processorid");
        String bios = getWmicValue("bios", "serialnumber");
        return sha256(mb + cpu + bios);
    }

    private static String getWmicValue(String namespace, String property) {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "wmic", namespace, "get", property });
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            sc.next(); // skip header
            String value = sc.nextLine().trim();
            sc.close();
            return value;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
}
