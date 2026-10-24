package com.mysteryvault;

/**
 * Mystery Vault Compatibility Launcher.
 *
 * Provides backwards compatibility for IDEs, scripts, or legacy configurations
 * that expect "com.mysteryvault.Main" as the main class.
 *
 * This class simply delegates immediately to the authoritative production
 * entry point and composition root: {@link com.mysteryvault.MysteryVaultApplication}.
 */
public class Main {

    public static void main(String[] args) {
        MysteryVaultApplication.main(args);
    }
}
