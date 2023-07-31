package com;

import com.honor.HonorService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
public class DraftService {

    @Autowired
    UserService userService;

    Set<MyUser> alreadyPicked = new HashSet<>();

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    GameProgressService gameProgress;

    int stage = 1;

    @Autowired
    private HonorService honorService;

    public void reset() {
        stage = 1;
        alreadyPicked.clear();
    }

    public Map getData() {

        Map res = new HashMap();

        var waitingFor = userService.findAll();
        waitingFor.removeAll(alreadyPicked);

        for (MyUser user : userService.findAll()) {

            if (alreadyPicked.contains(user)) {

                List components = new ArrayList();
                components.add(TextComponent.builder().fontSize(4).value("DRAFTING").build());
                components.add(TextComponent.builder().fontSize(2).value("Waiting For:").build());
                components.add(SingleReadOnlyListComponent.builder()
                        .list(waitingFor.stream().map(x -> x.getUsername()).toList())
                        .build());

                res.put(user.getUsername(), components);

            } else {

                List components = new ArrayList();
                components.add(TextComponent.builder().fontSize(4).value("DRAFTING").build());
                components.add(TextComponent.builder().fontSize(2).value(stage == 1 ? "Pick 1st Ninja Card" : "Pick 2nd Ninja Card").build());
                components.add(MultiComponents
                        .builder()
                        .ninjaCardComponents(
                                ninjaCardService.dealtMap().get(user)
                                        .stream().map(x -> NinjaCardComponent
                                                .builder()
                                                .ninjaCard(x)
                                                .url("game/draftpicked")
                                                .style(NinjaCardComponentStyle
                                                        .builder()
                                                        .build())
                                                .build())
                                        .toList())
                        .build());

                res.put(user.getUsername(), components);
            }
        }

        return res;

    }

    public void draftPicked(Principal principal, NinjaCard ninjaCard) {

        MyUser user = userService.findByUsername(principal.getName());

        alreadyPicked.add(user);

        ninjaCardService.addToHand(user, ninjaCard);
        MyUser nextUser = userService.findBySeatOrder(user.getSeatOrder() + 1);
        ninjaCardService.removeFromDealt(user, ninjaCard);
        ninjaCardService.addToOnHold(nextUser, ninjaCardService.dealtMap().get(user).toArray(NinjaCard[]::new));
        ninjaCardService.clearDealt(user);

        if (alreadyPicked.size() == userService.findAll().size()) {

            stage += 1;

            if (stage == 2) {
                alreadyPicked.clear();

                ninjaCardService.moveAllOnHoldToOnHand();

            } else {

                ninjaCardService.moveAllOnHoldToDiscarded();

//                ninjaCardService.debugMode();
//                honorService.debugMode();

                gameProgress.update(GameProgressData.SPY_MYSTIC);

            }

        }
    }

}
