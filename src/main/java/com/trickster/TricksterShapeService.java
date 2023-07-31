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

enum TricksterShapeStage {
    PICK_TWO_PLAYERS,
    SWAP_OR_NOT,
    SWAPPED
}

@Service
public class TricksterShapeService {

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
    TricksterShapeStage stage = TricksterShapeStage.PICK_TWO_PLAYERS;

    @Autowired
    private SelectorService selectorService;

    private boolean swapped;

    public void reset() {
        stage = TricksterShapeStage.PICK_TWO_PLAYERS;
        swapped = false;
    }

    public void getData(Map map) throws Exception {

        if (stage == TricksterShapeStage.PICK_TWO_PLAYERS)
            getPickTwoPlayers(map);
        else if (stage == TricksterShapeStage.SWAP_OR_NOT)
            getSwapOrNot(map);
        else if (stage == TricksterShapeStage.SWAPPED)
            getSwapped(map);

    }

    private void getPickTwoPlayers(Map map) throws Exception {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Shapeshifter (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Pick TWO Players. You may swap their HOUSE cards"
                                    : selectorService.getSelecting() + " is choosing two players..."
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
                                            put("id", "shape-confirmselection");
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

    private void getSwapOrNot(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Shapeshifter (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Swap " + String.join(" and ", selectorService.getSelected()) + "'s HOUSE cards?"
                                    : selectorService.getSelecting() + " saw " + String.join(" and ", selectorService.getSelected()) + "'s HOUSE cards!"
                    )
                    .build());

            if (!isInCharge)
                components.add(TextComponent.builder().fontSize(2)
                        .value("Deciding to swap or not...")
                        .build());

            if (isInCharge) {
                components.add(MultiComponents
                        .builder()
                        .numCols(2)
                        .houseCardComponents(Arrays.asList(
                                HouseCardComponent.builder()
                                        .owner(selectorService.getSelected().get(0))
                                        .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(0))))
                                        .build(),
                                HouseCardComponent.builder()
                                        .owner(selectorService.getSelected().get(1))
                                        .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(1))))
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
                                            put("id", "shape-swap");
                                            put("swap", true);
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("green")
                                                .title("Swap")
                                                .build())
                                        .build(),
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "shape-swap");
                                            put("swap", false);
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .color("red")
                                                .title("Don't Swap")
                                                .build())
                                        .build()))
                        .build());
            }

            map.put(user.getUsername(), components);

        }

    }

    private void getSwapped(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? swapped
                                    ? "You swapped "
                                    + String.join(" and ", selectorService.getSelected()) + "'s HOUSE cards"
                                    : "You Did Not Swap "
                                    + String.join(" and ", selectorService.getSelected()) + "'s HOUSE cards"
                                    : selectorService.getSelecting()
                                    + " may have swapped "
                                    + String.join(" and ", selectorService.getSelected())
                                    + "'s HOUSE cards"
                    )
                    .build());

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "shape-end");
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

        if (selectorService.getSelected().size() != 2)
            return;

        stage = TricksterShapeStage.SWAP_OR_NOT;
    }

    public void onSwap(Principal principal, Map body) throws Exception {
        swapped = (boolean) body.get("swap");

        if (swapped) {

            MyUser userA = userService.findByUsername(selectorService.getSelected().get(0));
            MyUser userB = userService.findByUsername(selectorService.getSelected().get(1));

            houseService.swapHouse(userA, userB);

        }

        stage = TricksterShapeStage.SWAPPED;
    }

    public void end(Principal principal) throws Exception {

        tricksterService.setStage(TricksterStage.PLAY_CARDS);
        tricksterService.end(principal);

    }


    public void setStage(TricksterShapeStage stage) {
        this.stage = stage;
    }
}
