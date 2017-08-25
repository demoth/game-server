package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.model.User;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UserService {
    private final Map<String, User> currentUsers = new HashMap<>();

    public User login(String user, String pass) {
        if ("demoth".equals(user))
            return new User();
        return null;
    }

    public String[] register(User user, String id) {
        currentUsers.put(id, user);
        String[] chars = new String[]{
                "Totemy [Templar lvl 12]",
                "ArkWi4ka [Witch lvl 2]"};
        return chars;
    }
}
