package com.lobby;

import com.GameScreenData;
import com.MyUser;
import com.MyWebSocketHandler;
import com.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LobbyService {

    @Autowired
    UserService userService;

    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    @Autowired
    MyWebSocketHandler webSocketHandler;

    public Map getLobbyData() throws Exception {

        List<MyUser> users = userService.findAllByOrderBySeatOrder();

        List<Map> playerList = new ArrayList<>();

        MyUser host = userService.findHost();

        for (MyUser u : users) {

            Map player = new HashMap<>();
            player.put("username", u.getUsername());
            player.put("hostUsername", host.getUsername());
            playerList.add(player);

        }

        Map responseMap = new HashMap<>();

        for (MyUser user : users) {

            Map map = new HashMap();
            map.put("screen", GameScreenData.LOBBY.toString());
            map.put("usernames", playerList);
            map.put("isHost", user.isHost());
            responseMap.put(user.getUsername(), map);

        }

        return responseMap;
    }

    public void onKick(Map requestBody, Principal principal) throws Exception {

        String kickUsername = (String) requestBody.get("username");

        MyUser kickUser = userService.findByUsername(kickUsername);

        if (kickUser.isHost())
            return;

        userService.delete(kickUser);
        webSocketHandler.disconnect(kickUsername);

        Map map = sessionRepository.findByPrincipalName(kickUsername);
        if (map != null && !map.isEmpty()) {

            Set<String> sessionIds = map.keySet();
            sessionIds.forEach(x -> sessionRepository.deleteById(x));

        }

        List<MyUser> all = userService.findAllByOrderBySeatOrder();

        // when kick, need to ensure seat order is recalculated
        int counter = 0;
        for (MyUser user : all)
            user.setSeatOrder(counter++);


    }

    public void move(Map body) throws Exception {

        boolean up = (boolean) body.get("up");
        MyUser target = userService.findByUsername((String) body.get("username"));

        if (up && target.getSeatOrder() == 0)
            return;

        if (!up && target.getSeatOrder() == userService.findAll().size() - 1)
            return;

        MyUser toSwap = userService.findBySeatOrder(up ? target.getSeatOrder() - 1 : target.getSeatOrder() + 1);

        int a = target.getSeatOrder();
        int b = toSwap.getSeatOrder();

        target.setSeatOrder(b);
        toSwap.setSeatOrder(a);


    }


}
