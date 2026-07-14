package org.example.utilities;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(11));
    }

    public boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
