package com.spymystic;

import com.*;
import com.house.HouseService;
import com.lifeanddeath.LifeAndDeathService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import com.trickster.TricksterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;

@Service
public class SpyMysticService {

    @Autowired
    UserService userService;

    @Autowired
    NinjaCardService ninjaCardService;
    SpyMysticStage stage = SpyMysticStage.PLAY;
    SpyOrMystic spyOrMystic = SpyOrMystic.SPY;
    @Autowired
    @Lazy
    TricksterService tricksterService;
    @Autowired
    SelectorService selectorService;
    NinjaCard seenNinjaCard;
    @Autowired
    private HouseService houseService;
    @Autowired
    private GameProgressService gameProgress;
    @Autowired
    private LifeAndDeathService lifeAndDeathService;
    private boolean activatedFromGrave;
    private Random random = new SecureRandom();

    public void reset() {
        stage = SpyMysticStage.PLAY;
        activatedFromGrave = false;
        spyOrMystic = SpyOrMystic.SPY;
        seenNinjaCard = null;
    }

    public Map getData() {

        Map res = new LinkedHashMap();

        if (stage == SpyMysticStage.PLAY)
            getPlay(res);
        else if (stage == SpyMysticStage.CHOOSE_TARGET_TO_LOOK)
            getChooseTargetToLook(res);
        else if (stage == SpyMysticStage.SAW_TARGET)
            getSawTarget(res);

        return res;

    }

    private void getPlay(Map res) {

        for (MyUser user : userService.findAll()) {

            List<NinjaCardComponent> onHand = ninjaCardService.allOnHand(user).stream().map(x -> NinjaCardComponent
                            .builder()
                            .url("game/play")
                            .ninjaCard(x)
                            .build())
                    .toList();

            List components = new ArrayList();
            components.add(TextComponent
                    .builder()
                    .fontSize(4)
                    .value(spyOrMystic == SpyOrMystic.SPY ? "SPIES" : "MYSTICS")
                    .build());
            components.add(TextComponent
                    .builder()
                    .fontSize(2)
                    .value("Play " + (spyOrMystic == SpyOrMystic.SPY ? "SPIES" : "MYSTICS"))
                    .build());
            components.add(MultiComponents
                    .builder()
                    .numCols(2)
                    .ninjaCardComponents(onHand)
                    .build());

            if (user.isHost())
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .url("game/btnclick")
                                        .body(new HashMap() {{
                                            put("id", "spy-mystic-end");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build()
                );


            res.put(user.getUsername(), components);

        }
    }

    private void getChooseTargetToLook(Map res) {

        List<MyUser> except = userService.findAllExcept(userService.findByUsername(selectorService.getSelecting()));

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();
            components.add(TextComponent
                    .builder()
                    .fontSize(4)
                    .value(spyOrMystic == SpyOrMystic.SPY ? "SPIES" : "MYSTIC")
                    .build());

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            if (isInCharge)
                components.add(TextComponent
                        .builder()
                        .fontSize(2)
                        .value("Look at a Player's " +
                                (spyOrMystic == SpyOrMystic.SPY ? "HOUSE Card" : "HOUSE and NINJA card")
                        )
                        .build());
            else {

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(selectorService.getSelecting() + " is deciding whose card to look...")
                        .build());
            }

            components.add(SingleSelectableListComponent
                    .builder()
                    .list(except.stream().map(x -> x.getUsername()).toList())
                    .selected(selectorService.getSelected())
                    .url(isInCharge ? "game/selectusername" : null)
                    .build());

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "spy-mystic-confirmselection");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .title("Next")
                                                .build())
                                        .build()
                        ))
                        .build());


            res.put(user.getUsername(), components);

        }

    }

    private void getSawTarget(Map res) {

        if (spyOrMystic == SpyOrMystic.MYSTIC && seenNinjaCard == null) {
            List<NinjaCard> all = ninjaCardService.onHandMap().get(userService.findByUsername(selectorService.lastSelected()));

            if (all.size() > 0) {
                Collections.shuffle(all, random);
                seenNinjaCard = all.get(0);
            }

        }

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();

            components.add(TextComponent.builder().fontSize(4).value(
                    spyOrMystic == SpyOrMystic.SPY ? "SPIES" : "MYSTICS"
            ).build());

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            if (isInCharge) {

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(selectorService.lastSelected() + "'s "
                                + (spyOrMystic == SpyOrMystic.SPY ? " HOUSE " : seenNinjaCard == null ? "" : " and NINJA ")
                                + "card(s): ")
                        .build());

                if (seenNinjaCard == null) {

                    components.add(MultiComponents
                            .builder()
                            .houseCardComponents(
                                    Arrays.asList(
                                            HouseCardComponent
                                                    .builder()
                                                    .houseCard(houseService.findByUser(userService.findByUsername(selectorService.lastSelected())))
                                                    .build()
                                    )
                            )
                            .numCols(spyOrMystic == SpyOrMystic.SPY ? 1 : seenNinjaCard == null ? 1 : 2)
                            .build());

                } else {

                    components.add(MultiComponents
                            .builder()
                            .houseCardComponents(
                                    Arrays.asList(
                                            HouseCardComponent
                                                    .builder()
                                                    .houseCard(houseService.findByUser(userService.findByUsername(selectorService.lastSelected())))
                                                    .build()
                                    )
                            ).ninjaCardComponents(
                                    spyOrMystic == SpyOrMystic.SPY ? null :

                                            seenNinjaCard == null
                                                    ? null
                                                    : Arrays.asList(
                                                    NinjaCardComponent
                                                            .builder()
                                                            .ninjaCard(seenNinjaCard)
                                                            .build()
                                            )
                            )
                            .numCols(spyOrMystic == SpyOrMystic.SPY ? 1 : seenNinjaCard == null ? 1 : 2)
                            .build());

                }

            } else
                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(selectorService.getSelecting() + " saw " + selectorService.lastSelected() + "'s HOUSE "
                                + (spyOrMystic == SpyOrMystic.SPY ? "" : seenNinjaCard == null
                                ? "" : " and NINJA ")
                                + "card")
                        .build());

            if (isInCharge)
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent
                                        .builder()
                                        .body(new HashMap() {{
                                            put("id", "spy-mystic-end");
                                        }})
                                        .style(ButtonComponentStyle
                                                .builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build());

            res.put(user.getUsername(), components);

        }

    }

    // =================================== ACTIONS ===================================

    public void play(Principal principal, NinjaCard ninjaCard) {

        MyUser user = userService.findByUsername(principal.getName());

        if (spyOrMystic == SpyOrMystic.SPY && !ninjaCard.getCategory().equals("spy"))
            return;
        else if (spyOrMystic == SpyOrMystic.MYSTIC && !ninjaCard.getCategory().equals("mystic"))
            return;

        Map<MyUser, NinjaCard> map = new HashMap();
        map.put(user, ninjaCard);
        ninjaCardService.addToBeActivate(map);
        ninjaCardService.removeFromHand(user, ninjaCard);

    }

    public void end(Principal principal) throws Exception {

        selectorService.reset(null);

        if (activatedFromGrave) {
            activatedFromGrave = false;
            tricksterService.resetFromGrave(principal);
            return;
        }

        if (ninjaCardService.getToBeActivated().isEmpty()) { // no one played

            if (spyOrMystic == SpyOrMystic.SPY) {
                resetForMystic();
            } else if (spyOrMystic == SpyOrMystic.MYSTIC) {
                tricksterService.reset();
                gameProgress.update(GameProgressData.TRICKSTER);
            }

        } else {

            Map<MyUser, NinjaCard> first = ninjaCardService.removeFirstFromGetToBeActivated();
            var kvp = first.entrySet().stream().findFirst().get();
            var user = kvp.getKey();
            var ninjaCard = kvp.getValue();
            ninjaCardService.addToUsed(user, ninjaCard);
            selectorService.reset(user.getUsername());
            seenNinjaCard = null;

            stage = SpyMysticStage.CHOOSE_TARGET_TO_LOOK;

        }

    }

    public void confirmSelection(Principal principal, Map body) throws Exception {

        if (selectorService.getSelected().isEmpty())
            return;

        stage = SpyMysticStage.SAW_TARGET;
    }

    public void setActivatedFromGrave(boolean activatedFromGrave) {
        this.activatedFromGrave = activatedFromGrave;

        if (activatedFromGrave)
            stage = SpyMysticStage.CHOOSE_TARGET_TO_LOOK;
    }

    public SpyOrMystic getSpyOrMystic() {
        return spyOrMystic;
    }

    public void setSpyOrMystic(SpyOrMystic e) {
        spyOrMystic = e;
    }

    public void resetForMystic() {
        stage = SpyMysticStage.PLAY;
        spyOrMystic = SpyOrMystic.MYSTIC;
        seenNinjaCard = null;
    }

    public void setStage(SpyMysticStage stage) {
        this.stage = stage;
    }
}