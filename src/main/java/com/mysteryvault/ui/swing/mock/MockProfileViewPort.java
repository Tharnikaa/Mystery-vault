package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.ProfileViewPort;
import com.mysteryvault.ui.swing.viewmodel.ProfileViewModel;

public class MockProfileViewPort implements ProfileViewPort {

    @Override
    public ProfileViewModel getPlayerProfile() {
        return new ProfileViewModel(
                "Detective",
                37,
                4,
                20,
                70,
                6,
                72
        );
    }
}
