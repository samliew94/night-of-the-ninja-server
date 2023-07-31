package com.trickster;

import com.*;
import com.lifeanddeath.JudgeBlindShinobi;
import com.lifeanddeath.JudgeBlindShinobiService;
import com.lifeanddeath.JudgeBlindShinobiStage;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import com.spymystic.SpyMysticService;
import com.spymystic.SpyMysticStage;
import com.spymystic.SpyOrMystic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;

@Service
public class TricksterGraveService {

    @Autowired
    UserService userService;

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    GameProgressService gameProgress;

    Random random = new SecureRandom();

    TricksterGraveStage tricksterGraveStage = TricksterGraveStage.SEE_TWO_CARDS;
    List<NinjaCard> twoCards = new ArrayList<>(); // what are the two cards available?
    @Autowired
    SpyMysticService spyMysticService;
    @Autowired
    @Lazy
    TricksterService tricksterService;

    @Autowired
    @Lazy
    TricksterShapeService shapeService;

    @Autowired
    TricksterSpiritService spiritService;
    @Autowired
    SelectorService selectorService;
    @Autowired
    private JudgeBlindShinobiService judgeBlindShinobiService;
    private NinjaCard selectedNinjaCard; // which card is being selected?
    @Autowired
    private TricksterTroubleService troubleService;

    @Autowired
    private TricksterThiefService thiefService;

    public void reset() {
        tricksterGraveStage = TricksterGraveStage.SEE_TWO_CARDS;
        twoCards.clear();
        selectedNinjaCard = null;
    }

    public void getData(Map map) {

        if (tricksterGraveStage == TricksterGraveStage.SEE_TWO_CARDS)
            seeTwoCards(map);
        else if (tricksterGraveStage == TricksterGraveStage.USE_OR_KEEP)
            useOrKeep(map);

    }

    public void setSelectedNinjaCard(Principal principal, NinjaCard ninjaCard) {
        this.selectedNinjaCard = ninjaCard;

        tricksterGraveStage = TricksterGraveStage.USE_OR_KEEP;
    }

    private void seeTwoCards(Map map) {

        if (twoCards.isEmpty()) {
            List<NinjaCard> discards = ninjaCardService.getDiscards();
            Collections.shuffle(discards, random);
            twoCards.addAll(discards);
        }

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Grave Digger (Trickster)").build());

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            components.add(TextComponent.builder().fontSize(2).value(isInCharge ?
                            "Pick ONE Card- use now or later" :
                            selectorService.getSelecting() + " is picking a card from discards... ")
                    .build());

            if (isInCharge) {

                components.add(MultiComponents
                        .builder()
                        .numCols(2)
                        .ninjaCardComponents(
                                List.of(
                                        NinjaCardComponent
                                                .builder()
                                                .ninjaCard(twoCards.get(0))
                                                .url("game/selectninjacard")
                                                .style(NinjaCardComponentStyle
                                                        .builder()
                                                        .build())
                                                .build(),
                                        NinjaCardComponent
                                                .builder()
                                                .ninjaCard(twoCards.get(1))
                                                .url("game/selectninjacard")
                                                .style(NinjaCardComponentStyle
                                                        .builder()
                                                        .build())
                                                .build()
                                )
                        )
                        .build());

            }

            map.put(user.getUsername(), components);

        }

    }

    private void useOrKeep(Map map) {

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Grave Digger (Trickster)").build());

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            components.add(TextComponent.builder().fontSize(2).value(isInCharge ?
                            "Use Now, Keep or Discard?" :
                            selectorService.getSelecting() + " is deciding whether to use, keep or discard... ")
                    .build());

            if (isInCharge) {

                components.add(MultiComponents
                        .builder()
                        .numCols(1)
                        .ninjaCardComponents(
                                Arrays.asList(
                                        NinjaCardComponent
                                                .builder()
                                                .ninjaCard(selectedNinjaCard)
                                                .build()

                                )
                        )
                        .build());

                List<ButtonComponent> buttonComponents = new ArrayList<>();

                if (!selectedNinjaCard.getCategory().equals("react")) {
                    buttonComponents.add(ButtonComponent.builder()
                            .url("game/btnclick")
                            .body(new HashMap() {{
                                put("id", "grave-useorkeep");
                                put("useOrKeep", true);
                            }})
                            .style(ButtonComponentStyle.builder()
                                    .title("Use Now")
                                    .color("green")
                                    .build())
                            .build());
                }

                boolean impossibleToKeep = selectedNinjaCard.getCategory().equals("spy")
                        || selectedNinjaCard.getCategory().equals("mystic")
                        || selectedNinjaCard.getCategory().equals("trickster");

                buttonComponents.add(ButtonComponent.builder()
                        .url("game/btnclick")
                        .body(new HashMap() {{
                            put("id", "grave-useorkeep");
                            put("useOrKeep", false);
                        }})
                        .style(ButtonComponentStyle.builder()
                                .title(impossibleToKeep ? "Discard" : "Keep")
                                .color("orange")
                                .build())
                        .build());

                components.add(MultiButtonComponents
                        .builder()
                        .numCols(buttonComponents.size())
                        .buttonComponents(buttonComponents)
                        .build()

                );

            }

            map.put(user.getUsername(), components);

        }

    }

    public void onUseOrKeep(Principal principal, Map body) throws Exception {
        boolean useOrkeep = (boolean) body.get("useOrKeep");

        if (useOrkeep)
            use(principal);
        else
            keep(principal);
    }

    private void keep(Principal principal) throws Exception {

        String category = selectedNinjaCard.getCategory();
        String code = selectedNinjaCard.getCode();
        int executionOrder = selectedNinjaCard.getExecutionOrder();

        NinjaCard discard = ninjaCardService.findFromDiscards(category, code, executionOrder);
        ninjaCardService.removefromDiscards(discard);

        if (!(category.equals("spy") || category.equals("mystic") || category.equals("trickster"))) // too late to play
            ninjaCardService.addToHand(userService.findByUsername(principal.getName()), discard);

        tricksterService.setStage(TricksterStage.PLAY_CARDS);
        tricksterService.end(principal);

    }

    private void use(Principal principal) throws Exception {

        selectorService.reset(principal.getName());

        if (selectedNinjaCard == null)
            return;

        String category = selectedNinjaCard.getCategory();
        String code = selectedNinjaCard.getCode();
        int executionOrder = selectedNinjaCard.getExecutionOrder();

        ninjaCardService.addToUsed(userService.findByUsername(principal.getName()), selectedNinjaCard);

        if (category.equals("spy") || category.equals("mystic")) {

            spyMysticService.setActivatedFromGrave(true);
            spyMysticService.setSpyOrMystic(category.equals("spy") ? SpyOrMystic.SPY : SpyOrMystic.MYSTIC);
            spyMysticService.setStage(SpyMysticStage.CHOOSE_TARGET_TO_LOOK);

            gameProgress.update(GameProgressData.SPY_MYSTIC);

        } else if (category.equals("trickster") && !code.equals("judge")) {

            if (code.equals("shape")) {
                tricksterService.setStage(TricksterStage.ACTIVATE_SHAPE);
                shapeService.setStage(TricksterShapeStage.PICK_TWO_PLAYERS);
            } else if (code.equals("trouble")) {
                tricksterService.setStage(TricksterStage.ACTIVATE_TROUBLE);
                troubleService.setStage(TricksterTroubleStage.PICK_A_PLAYER);
            } else if (code.equals("spirit")) {
                tricksterService.setStage(TricksterStage.ACTIVATE_SPIRIT);
                spiritService.setStage(TricksterSpiritStage.PICK_A_PLAYER);
            } else if (code.equals("thief")) {
                tricksterService.setStage(TricksterStage.ACTIVATE_THIEF);
                thiefService.setStage(TricksterThiefStage.REVEAL_HOUSE_AND_PICK_A_PLAYER);
            }

        } else if (code.equals("judge") || code.equals("blind") || code.equals("shinobi")) {

            if (code.equals("judge"))
                tricksterService.setStage(TricksterStage.ACTIVATE_JUDGE);

            judgeBlindShinobiService.setJudgeOrBlindOrShinobi(
                    code.equals("judge")
                            ? JudgeBlindShinobi.JUDGE
                            : code.equals("blind")
                            ? JudgeBlindShinobi.BLIND
                            : JudgeBlindShinobi.SHINOBI
            );

            if (code.equals("blind") || code.equals("shinobi")) {

                judgeBlindShinobiService.setActivatedFromGrave(true);
                judgeBlindShinobiService.setStage(JudgeBlindShinobiStage.CHOOSE_TARGET);


                if (code.equals("blind"))
                    gameProgress.update(GameProgressData.BLIND);
                else if (code.equals("shinobi"))
                    gameProgress.update(GameProgressData.SHINOBI);

            }


        }

    }


}
