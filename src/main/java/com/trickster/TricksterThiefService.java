package com.trickster;

import com.GameProgressService;
import com.MyUser;
import com.SelectorService;
import com.UserService;
import com.honor.HonorService;
import com.house.HouseService;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

enum TricksterThiefStage {
    REVEAL_HOUSE_AND_PICK_A_PLAYER, // reveal self HOUSE card and show other players
    STOLE
}

@Service
public class TricksterThiefService {

    @Autowired
    UserService userService;

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    GameProgressService gameProgress;

    @Autowired
    @Lazy
    TricksterService tricksterService;

    @Autowired
    HouseService houseService;
    TricksterThiefStage stage = TricksterThiefStage.REVEAL_HOUSE_AND_PICK_A_PLAYER;
    @Autowired
    private HonorService honorService;

    @Autowired
    private SelectorService selectorService;

    public void reset() {
        stage = TricksterThiefStage.REVEAL_HOUSE_AND_PICK_A_PLAYER;
    }

    public void getData(Map map) throws Exception {

        if (stage == TricksterThiefStage.REVEAL_HOUSE_AND_PICK_A_PLAYER)
            getRevealHouseAndPickAPlayer(map);
        else if (stage == TricksterThiefStage.STOLE)
            getStole(map);

    }

    private void getRevealHouseAndPickAPlayer(Map map) throws Exception {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());
        //        honorService.giveRandomHonorToken(userService.findAllExcept(userInCharge).get(0)); // debug mode

        List<MyUser> allGreaterThan = honorService.findAllWithTotalTokensGreaterThan(userInCharge);

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Thief (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Pick a player to steal HONOR token from."
                                    : selectorService.getSelecting() + " is deciding whose HONOR token to steal"

                    )
                    .build());

            if (isInCharge && allGreaterThan.size() == 0) {
                components.add(TextComponent.builder().fontSize(2)
                        .value("Can't steal. No one has more tokens than you")
                        .build());
            }

            if (!isInCharge) {

                components.add(TextComponent.builder().fontSize(2)
                        .value(selectorService.getSelecting() + " reveals HOUSE card:")
                        .build());

                components.add(MultiComponents
                        .builder()
                        .numCols(1)
                        .houseCardComponents(
                                Arrays.asList(HouseCardComponent
                                        .builder()
                                        .houseCard(houseService.findByUser(userInCharge))
                                        .build())
                        )
                        .build());

            }

            components.add(SingleSelectableListComponent
                    .builder()
                    .list(allGreaterThan.stream().map(x -> x.getUsername()).toList())
                    .url(isInCharge ? "game/selectusername" : null)
                    .selected(selectorService.getSelected())
                    .build());

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "thief-confirmselection");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("blue")
                                                .title("Next")
                                                .build())
                                        .build()
                        ))
                        .build());

            map.put(user.getUsername(), components);

        }

    }

    private void getStole(Map map) throws Exception {

        boolean stole = selectorService.getSelected().size() > 0;

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Thief (Trickster)").build());

            if (isInCharge) {

                components.add(TextComponent.builder().fontSize(2)
                        .value(stole ?
                                "You stole an HONOR token from " + selectorService.getSelected().get(0) + ". "
                                        + "You now have "
                                        + honorService.getNumHonor(user)
                                        + " tokens with total sum of "
                                        + honorService.findSumOfTokens(user) + " points"
                                : "You did not steal from anyone"

                        )
                        .build());

            } else {

                components.add(TextComponent.builder().fontSize(2)
                        .value(stole ?
                                selectorService.getSelecting() + " stole an HONOR token from " + selectorService.getSelected().get(0)
                                : selectorService.getSelecting() + " did not steal from anyone"
                        )
                        .build());

            }

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "thief-end");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build());

            map.put(user.getUsername(), components);

        }

    }

    // ========================================== ACTIONS ==========================================

    public void confirmSelection() {

        if (!selectorService.getSelected().isEmpty()) { // didn't STEAL

            var from = userService.findByUsername(selectorService.getSelected().get(0));
            var to = userService.findByUsername(selectorService.getSelecting());
            honorService.takeRandomHonorTokenFromAndGiveTo(from, to);

        }

        stage = TricksterThiefStage.STOLE;
    }

    public void end(Principal principal) throws Exception {

        tricksterService.setStage(TricksterStage.PLAY_CARDS);
        tricksterService.end(principal);

    }


    public void setStage(TricksterThiefStage stage) {
        this.stage = stage;
    }
}
