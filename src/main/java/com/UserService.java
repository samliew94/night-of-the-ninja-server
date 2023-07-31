package com;

import com.props.ButtonComponent;
import com.props.ButtonComponentStyle;
import com.props.MultiButtonComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class UserService {

    @Autowired
    MyUserRepository userRepository;

    public Map getUsersInLobby() throws Exception {

        List<MyUser> users = userRepository.findAll();

        List<Map> playerList = new ArrayList<>();

        Map data = new HashMap<>();
        data.put("screen", GameScreenData.LOBBY.toString());
        data.put("players", playerList);

        Map responseMap = new HashMap<>();

        for (MyUser user : users) {

            Map map = new HashMap();
            map.put("isHost", user.isHost());
            responseMap.put(user.getUsername(), map);

        }

        return responseMap;

    }

    public List<MyUser> findAll() {
        // TODO Auto-generated method stub
        return userRepository.findAll();
    }

    public Set<MyUser> findAllAsSet() {
        return findAll().stream().collect(Collectors.toSet());
    }

    public boolean isPrincipalHost(Principal principal) throws Exception {

        MyUser user = userRepository.findByUsername(principal.getName());

        return user.isHost();

    }

    public int totalPlayers() {
        // TODO Auto-generated method stub
        return userRepository.findAll().size();
    }

    public MyUser findByUsername(String username) {
        // TODO Auto-generated method stub
        return userRepository.findByUsername(username);
    }

    public MyUser findHost() {
        // TODO Auto-generated method stub
        return userRepository.findByIsHost(true);

    }

    public MyUser findBySeatOrder(int seatOrder) {

        MyUser user = userRepository.findBySeatOrder(seatOrder);

        if (user == null)
            return userRepository.findBySeatOrder(0);

        return user;
    }

    public List<MyUser> findAllByOrderBySeatOrder() {
        return userRepository.findAllByOrderBySeatOrder();
    }

    public List<MyUser> findAllByUsernameIn(Set<String> usernames) {
        return userRepository.findAllByUsernameIn(usernames);
    }

    public List<MyUser> findAllExcept(MyUser user) {
        return userRepository.findAllByNot(user);
    }

    public List<MyUser> findByNotIn(Collection<MyUser> users) {
        return userRepository.findByUserNotIn(users);
    }

    public void delete(MyUser user) throws Exception {
        userRepository.delete(user);
    }

    public MyUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findByUsername((String) principal);
    }

    public void addHardResetComponent(Map data) {

        var components = MultiButtonComponents
                .builder()
                .numCols(1)
                .buttonComponents(Arrays.asList(ButtonComponent.builder()
                        .body(new HashMap() {{
                            put("id", "hardreset");
                        }})
                        .style(ButtonComponentStyle.builder()
                                .title("Restart (Hard Reset)")
                                .color("red")
                                .build())
                        .build()))
                .build();

        data.forEach((o, o2) -> {
            var list = (List) o2;

            MyUser user = findByUsername((String) o);

            if (user.isHost())
                list.add(0, components);
        });


    }
}
