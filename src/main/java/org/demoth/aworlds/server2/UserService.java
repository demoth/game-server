package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.model.User;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public List<String> register(User user, String id) {
        currentUsers.put(id, user);
        ArrayList<String> chars = new ArrayList<>();
        chars.add("Totemy [Templar lvl 12]");
        chars.add("ArkWi4ka [Witch lvl 2]");
        return chars;
    }
}
