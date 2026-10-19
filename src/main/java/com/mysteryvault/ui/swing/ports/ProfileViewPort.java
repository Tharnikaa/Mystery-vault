package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.ProfileViewModel;

/**
 * Presentation port for Detective Profile, XP progress, and historical stats.
 * Domain ownership pending team approval; implemented via mock adapter for frontend standalone execution.
 */
public interface ProfileViewPort {
    ProfileViewModel getPlayerProfile();
}
