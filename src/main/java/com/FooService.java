package com;

import com.props.TextComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FooService {

    @Autowired
    UserService userService;

    public Map getData() throws Exception {
        Map res = new HashMap();

        for (MyUser user : userService.findAll()) {
            res.put(user.getUsername(), List.of(
                    TextComponent.builder().fontSize(4).value("TBD").build()
            ));
        }

        return res;
    }

}
