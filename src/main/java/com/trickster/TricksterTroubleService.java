package com.trickster;

import com.GameProgressService;
import com.MyUser;
import com.SelectorService;
import com.UserService;
import com.house.HouseService;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

enum TricksterTroubleStage {
    PICK_A_PLAYER,
    REVEAL_OR_NOT,
    REVEALED
}

@Service
public class TricksterTroubleService {

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
    TricksterTroubleStage stage = TricksterTroubleStage.PICK_A_PLAYER;
    @Autowired
    private SelectorService selectorService;
    private boolean revealed;

    public void reset() {
        stage = TricksterTroubleStage.PICK_A_PLAYER;
        revealed = false;
    }

    public void getData(Map map) throws Exception {

        if (stage == TricksterTroubleStage.PICK_A_PLAYER)
            getPickAPlayer(map);
        else if (stage == TricksterTroubleStage.REVEAL_OR_NOT)
            getRevealOrNot(map);
        else if (stage == TricksterTroubleStage.REVEALED)
            getRevealed(map);

    }

    private void getPickAPlayer(Map map) throws Exception {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Troublemaker (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Look at another player's HOUSE card. You may reveal it."
                                    : selectorService.getSelecting() + " is thinking..."
                    )
                    .build());

            components.add(SingleSelectableListComponent
                    .builder()
                    .list(userService.findAllExcept(userInCharge).stream().map(x -> x.getUsername()).toList())
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
                                            put("id", "trouble-confirmselection");
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

    private void getRevealOrNot(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Troublemaker (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Reveal " + selectorService.getSelected().get(0) + "'s HOUSE card?"
                                    : selectorService.getSelecting() + " saw " + selectorService.getSelected().get(0) + "'s HOUSE card!"
                    )
                    .build());

            if (!isInCharge)
                components.add(TextComponent.builder().fontSize(2)
                        .value("Deciding to reveal or not...")
                        .build());

            if (isInCharge) {
                components.add(MultiComponents
                        .builder()
                        .numCols(1)
                        .houseCardComponents(Arrays.asList(
                                HouseCardComponent.builder()
                                        .owner(selectorService.getSelected().get(0))
                                        .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(0))))
                                        .build()
                        ))
                        .build()
                );

                components.add(MultiButtonComponents
                        .builder()
                        .numCols(2)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "trouble-reveal");
                                            put("reveal", true);
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("green")
                                                .title("Reveal")
                                                .build())
                                        .build(),
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "trouble-reveal");
                                            put("reveal", false);
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("red")
                                                .title("Don't Reveal")
                                                .build())
                                        .build()))
                        .build());
            }

            map.put(user.getUsername(), components);

        }

    }

    private void getRevealed(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Troublemaker (Trickster)").build());

            if (revealed) {
                components.add(TextComponent.builder().fontSize(2)
                        .value(selectorService.getSelecting() + " reveals " + selectorService.getSelected().get(0) + "'s HOUSE card...")
                        .build());

                components.add(MultiComponents
                        .builder()
                        .numCols(1)
                        .houseCardComponents(Arrays.asList(
                                HouseCardComponent.builder()
                                        .owner(selectorService.getSelected().get(0))
                                        .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(0))))
                                        .build()
                        ))
                        .build());
            } else {

                components.add(TextComponent.builder().fontSize(2)
                        .value(selectorService.getSelecting() + " chose not to reveal " + selectorService.getSelected().get(0) + "'s HOUSE card")
                        .build());

            }

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "trouble-end");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("blue")
                                                .title("Next")
                                                .build())
                                        .build()))
                        .build());

            map.put(user.getUsername(), components);

        }

    }

    // ========================================== ACTIONS ==========================================
    public void confirmSelection() {

        if (selectorService.getSelected().size() != 1)
            return;

        stage = TricksterTroubleStage.REVEAL_OR_NOT;
    }

    public void reveal(Principal principal, Map body) throws Exception {
        revealed = (boolean) body.get("reveal");
        stage = TricksterTroubleStage.REVEALED;
    }

    public void end(Principal principal) throws Exception {

        tricksterService.setStage(TricksterStage.PLAY_CARDS);
        tricksterService.end(principal);

    }


    public void setStage(TricksterTroubleStage stage) {
        this.stage = stage;
    }
}
