package uk.co.nevarneyok.interfaces;

import uk.co.nevarneyok.entities.User;

/**
 * Interface declaring methods for login dialog.
 */
public interface LoginDialogInterface {

    void successfulLoginOrRegistration(User user);

}
