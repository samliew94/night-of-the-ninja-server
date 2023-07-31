package com.trickster;

import com.*;
import com.lifeanddeath.JudgeBlindShinobiService;
import com.lifeanddeath.LifeAndDeathService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
public class TricksterService {

    @Autowired
    UserService userService;

    @Autowired
    NinjaCardService ninjaCardService;
    TricksterStage stage = TricksterStage.PLAY_CARDS;
    @Autowired
    SelectorService selectorService;
    @Autowired
    private GameProgressService gameProgress;
    @Autowired
    private TricksterShapeService shapeService;
    @Autowired
    private TricksterGraveService graveService;
    @Autowired
    private TricksterTroubleService troubleService;
    @Autowired
    private TricksterSpiritService spiritService;
    @Autowired
    private TricksterThiefService thiefService;
    @Autowired
    private LifeAndDeathService lifeAndDeathService;
    @Autowired
    private JudgeBlindShinobiService judgeBlindShinobiService;

    public void reset() throws Exception {
        stage = TricksterStage.PLAY_CARDS;
        shapeService.reset();
        graveService.reset();
        troubleService.reset();
        spiritService.reset();
        thiefService.reset();
        judgeBlindShinobiService.reset();
    }

    public Map getData() throws Exception {

        Map res = new HashMap();

        if (stage == TricksterStage.PLAY_CARDS)
            getPlay(res);
        else if (stage == TricksterStage.ACTIVATE_SHAPE)
            shapeService.getData(res);
        else if (stage == TricksterStage.ACTIVATE_GRAVE)
            graveService.getData(res);
        else if (stage == TricksterStage.ACTIVATE_TROUBLE)
            troubleService.getData(res);
        else if (stage == TricksterStage.ACTIVATE_SPIRIT)
            spiritService.getData(res);
        else if (stage == TricksterStage.ACTIVATE_THIEF)
            thiefService.getData(res);
        else if (stage == TricksterStage.ACTIVATE_JUDGE) {
            res = judgeBlindShinobiService.getData();
        }

        return res;

    }

    private void getPlay(Map res) throws Exception {

        for (MyUser user : userService.findAll()) {

            List<NinjaCardComponent> onHand = ninjaCardService.allOnHand(user).stream().map(x -> NinjaCardComponent
                            .builder()
                            .url(x.getCategory().equals("trickster") ? "game/play" : null)
                            .ninjaCard(x)
                            .build())
                    .toList();

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("TRICKSTERS").build());
            components.add(TextComponent.builder().fontSize(2).value("Play Tricksters").build());
            components.add(MultiComponents
                    .builder()
                    .numCols(2)
                    .ninjaCardComponents(onHand)
                    .build());

            if (user.isHost()) {
                components.add(MultiButtonComponents.builder()
                        .buttonComponents(Arrays.asList(ButtonComponent.builder()
                                .body(new HashMap<>() {{
                                    put("id", "trickster-end");
                                }})
                                .style(ButtonComponentStyle.builder()
                                        .title("Next")
                                        .color("blue")
                                        .build())
                                .build()))
                        .build());

            }

            res.put(user.getUsername(), components);

        }
    }

    // ================================ ACTIONS ================================

    public void play(Principal principal, NinjaCard ninjaCard) {

        MyUser user = userService.findByUsername(principal.getName());

        if (lifeAndDeathService.isDead(user, true))
            return;
        else if (!ninjaCard.getCategory().equals("trickster"))
            return;

        if (ninjaCardService.tooLateToPlay(ninjaCard))
            return; // can't play lesser order trickster card.

        Map map = new HashMap();
        map.put(user, ninjaCard);
        ninjaCardService.addToBeActivate(map);
        ninjaCardService.removeFromHand(user, ninjaCard);

    }

    // ============================ OTHER TRICKSTERS ============================

    public void selectNinjaCard(Principal principal, NinjaCard ninjaCard) {

        if (stage == TricksterStage.ACTIVATE_GRAVE)
            graveService.setSelectedNinjaCard(principal, ninjaCard);

    }

    public void setStage(TricksterStage stage) {
        this.stage = stage;
    }

    public void end(Principal principal) throws Exception {

        selectorService.reset(null);

        if (stage == TricksterStage.PLAY_CARDS) {

            if (ninjaCardService.getToBeActivated().isEmpty()) {

                judgeBlindShinobiService.resetForBlind();
                gameProgress.update(GameProgressData.BLIND);

            } else {

                Map<MyUser, NinjaCard> first = ninjaCardService.removeFirstFromGetToBeActivated();
                var kvp = first.entrySet().stream().findFirst().get();
                var user = kvp.getKey();
                var ninjaCard = kvp.getValue();
                ninjaCardService.addToUsed(user, ninjaCard);
                selectorService.reset(user.getUsername());

                if (ninjaCard.getCode().equals("shape")) {
                    stage = TricksterStage.ACTIVATE_SHAPE;
                    shapeService.reset();
                } else if (ninjaCard.getCode().equals("grave")) {
                    stage = TricksterStage.ACTIVATE_GRAVE;
                    graveService.reset();
                } else if (ninjaCard.getCode().equals("trouble")) {
                    stage = TricksterStage.ACTIVATE_TROUBLE;
                    troubleService.reset();
                } else if (ninjaCard.getCode().equals("spirit")) {
                    stage = TricksterStage.ACTIVATE_SPIRIT;
                    spiritService.reset();
                } else if (ninjaCard.getCode().equals("thief")) {
                    stage = TricksterStage.ACTIVATE_THIEF;
                    thiefService.reset();
                } else if (ninjaCard.getCode().equals("judge")) {
                    stage = TricksterStage.ACTIVATE_JUDGE;
                    judgeBlindShinobiService.reset();
                }


            }

        }

    }

    // ****************************** RESETS ******************************

    public void resetFromGrave(Principal principal) throws Exception {

        stage = TricksterStage.PLAY_CARDS;
        gameProgress.update(GameProgressData.TRICKSTER);
        end(principal);

    }


    public TricksterStage getTricksterStage() {
        return stage;
    }
}

