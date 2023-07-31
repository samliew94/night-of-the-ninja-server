package com.lobby;

import com.GameController;
import com.GameProgressData;
import com.GameProgressService;
import com.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("lobby")
public class LobbyController {
    @Autowired
    UserService userService;

    @Autowired
    GameProgressService gameProgressService;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    @Lazy
    GameController gameController;

    @PostMapping("")
    public void toLobby(HttpServletRequest request, Principal principal) throws Exception {

        System.err.println("lobby/tolobby");

        if (!userService.isPrincipalHost(principal))
            return;

        gameProgressService.update(GameProgressData.LOBBY);
    }

    @PostMapping("kick")
    public void kick(@RequestBody Map requestMap, Principal principal) throws Exception {

        System.err.println("game/kick");

        if (!userService.isPrincipalHost(principal))
            return;

        lobbyService.onKick(requestMap, principal);

        gameController.update();

    }

    @PostMapping("move")
    public void move(@RequestBody Map body, Principal principal) throws Exception {

        System.err.println("game/move");

        if (!userService.isPrincipalHost(principal))
            return;

        lobbyService.move(body);

        gameController.update();

    }


}
