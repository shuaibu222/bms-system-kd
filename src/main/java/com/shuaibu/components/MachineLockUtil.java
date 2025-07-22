package com.shuaibu.components;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class MachineLockUtil {

    private static final File LOCK_FILE = getLockFile();

    public static boolean verifyMachine() {
        try {
            String fingerprint = getMachineFingerprint();

            if (LOCK_FILE.exists()) {
                String stored = Files.readString(LOCK_FILE.toPath()).trim();
                return stored.equals(fingerprint);
            } else {
                Files.writeString(LOCK_FILE.toPath(), fingerprint);
                hideFile(LOCK_FILE); // ✅ Make hidden
                LOCK_FILE.setReadOnly(); // ✅ Make read-only
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

    private static File getLockFile() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".premium_machine.lock"); // dot = hidden on Linux/macOS
    }

    private static void hideFile(File file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                Process p = Runtime.getRuntime().exec(
                        new String[] { "attrib", "+H", file.getAbsolutePath() });
                p.waitFor();
            }
            // Dot-prefix makes it hidden on Unix/macOS already
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
