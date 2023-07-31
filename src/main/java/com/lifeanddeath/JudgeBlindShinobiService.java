package com.lifeanddeath;

import com.*;
import com.house.HouseService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import com.trickster.TricksterService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
public class JudgeBlindShinobiService {

    @Autowired
    UserService userService;

    @Autowired
    LifeAndDeathService lifeAndDeathService;

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    HouseService houseService;

    @Autowired
    GameProgressService gameProgress;

    @Autowired
    @Lazy
    private TricksterService tricksterService;

    @Autowired
    private SelectorService selectorService;

    private JudgeBlindShinobiStage stage = JudgeBlindShinobiStage.CHOOSE_TARGET;
    private JudgeBlindShinobi judgeOrBlindOrShinobi = JudgeBlindShinobi.JUDGE;
    private boolean activatedFromGrave;

    public void reset() {
        stage = JudgeBlindShinobiStage.CHOOSE_TARGET;
        judgeOrBlindOrShinobi = JudgeBlindShinobi.JUDGE;
        activatedFromGrave = false;
    }

    public Map getData() {

        Map map = new LinkedHashMap();

        if (stage == JudgeBlindShinobiStage.PLAY)
            getPlay(map);
        if (stage == JudgeBlindShinobiStage.CHOOSE_TARGET)
            getChooseTarget(map);
        if (stage == JudgeBlindShinobiStage.SHINOBI_SEE_HOUSE_CARD)
            getShinobiSeeHouseCard(map);
        else if (stage == JudgeBlindShinobiStage.OUTCOME)
            getOutcome(map);


        return map;
    }

    private void getPlay(Map map) {

        for (MyUser user : userService.findAll()) {

            List<NinjaCardComponent> onHand = ninjaCardService.allOnHand(user).stream().map(x -> NinjaCardComponent
                            .builder()
                            .url(
                                    x.getCode().equals("blind") && judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND
                                            || x.getCode().equals("shinobi") && judgeOrBlindOrShinobi == JudgeBlindShinobi.SHINOBI
                                            ? "game/play" : null)
                            .ninjaCard(x)
                            .build())
                    .toList();

            List components = new ArrayList();
            components.add(TextComponent
                    .builder()
                    .fontSize(4)
                    .value(judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND ? "BLIND ASSASSINS" : "SHINOBIS")
                    .build());
            components.add(TextComponent
                    .builder()
                    .fontSize(2)
                    .value("Play " + (judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND ? "Blind Assassins" : "Shinobis"))
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
                                            put("id", "jbs-end");
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


            map.put(user.getUsername(), components);

        }
    }


    private void getChooseTarget(Map map) {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        List<MyUser> stillAlives = lifeAndDeathService.findAllByIsDead(false);
        stillAlives.remove(userInCharge);

        for (MyUser user : userService.findAll()) {

            boolean isUserInCharge = user.getUsername().equals(selectorService.getSelecting());

            List components = new ArrayList();

            String title = judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE
                    ? "Judge (Trickster)"
                    : judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND ? "Blind Assassin" : "Shinobi";

            if (isUserInCharge) {

                components.add(TextComponent.builder()
                        .fontSize(4)
                        .value(title)
                        .build());

                String desc = judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE
                        ? "KILL a player. Mirror Monk and Martyr have no effect."
                        : judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND ? "Kill a Player"
                        : "Look at a player's HOUSE card. You MAY KILL them";

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(desc)
                        .build());


            } else {

                String desc = selectorService.getSelecting() + " is choosing a target to KILL.";

                if (judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE)
                    desc += " Revealing HOUSE card...";

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(desc)
                        .build());

                if (judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE) {

                    components.add(MultiComponents.builder()
                            .numCols(1)
                            .houseCardComponents(Arrays.asList(
                                    HouseCardComponent.builder()
                                            .houseCard(houseService.findByUser(userInCharge))
                                            .owner(selectorService.getSelecting())
                                            .build()
                            ))
                            .build());

                }
            }

            components.add(SingleSelectableListComponent
                    .builder()
                    .list(stillAlives.stream().map(x -> x.getUsername()).toList())
                    .url(isUserInCharge ? "game/selectusername" : null)
                    .selected(selectorService.getSelected())
                    .build());

            if (isUserInCharge) {
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", judgeOrBlindOrShinobi == JudgeBlindShinobi.SHINOBI
                                                    ? stillAlives.isEmpty() ? "kill" : "jbs-shinobiseehousecard"
                                                    : "kill");
                                        }})
                                        .style(
                                                ButtonComponentStyle.builder()
                                                        .title("Next")
                                                        .color("blue")
                                                        .build()
                                        )
                                        .build()
                        ))
                        .build());
            }
            map.put(user.getUsername(), components);

        }

    }

    private void getShinobiSeeHouseCard(Map map) {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        for (MyUser user : userService.findAll()) {

            boolean isUserInCharge = user.getUsername().equals(selectorService.getSelecting());

            List components = new ArrayList();

            components.add(TextComponent.builder()
                    .fontSize(4)
                    .value("SHINOBI")
                    .build());

            if (isUserInCharge) {

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(selectorService.getSelected().get(0) + "'s HOUSE card is... ")
                        .build());

                components.add(
                        MultiComponents
                                .builder()
                                .numCols(1)
                                .houseCardComponents(Arrays.asList(
                                        HouseCardComponent.builder()
                                                .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(0))))
                                                .build()
                                ))
                                .build());

                components.add(MultiButtonComponents
                        .builder()
                        .numCols(2)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "kill");
                                            put("kill", true);
                                        }})
                                        .style(
                                                ButtonComponentStyle.builder()
                                                        .title("Kill")
                                                        .color("green")
                                                        .build()
                                        )
                                        .build(),
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "kill");
                                            put("kill", false);
                                        }})
                                        .style(
                                                ButtonComponentStyle.builder()
                                                        .title("Don't Kill")
                                                        .color("red")
                                                        .build()
                                        )
                                        .build()
                        ))
                        .build());

            } else {

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(selectorService.getSelecting() + " saw " + selectorService.getSelected().get(0) + "'s HOUSE card.")
                        .build());

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value("Deciding whether to kill " + selectorService.getSelected().get(0))
                        .build());

            }

            map.put(user.getUsername(), components);

        }

    }

    private void getOutcome(Map map) {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        LastKillResult latestKill = lifeAndDeathService.getLatestKill();

        for (MyUser user : userService.findAll()) {

            boolean isUserInCharge = user.getUsername().equals(selectorService.getSelecting());

            List components = new ArrayList();

            components.add(TextComponent.builder()
                    .fontSize(4)
                    .value(judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE
                            ? "Judge (Trickster)"
                            : judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND
                            ? "Blind Assassin"
                            : "Shinobi")
                    .build());

            if (latestKill.getActuallyDead() == null) { // no one was killed

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(latestKill.getKiller().getUsername()
                                + (latestKill.getIntendedTarget() == null
                                ? " can't KILL anyone cause there's no one to kill.."
                                : " allowed " + latestKill.getIntendedTarget().getUsername() + " to live another day.")
                        )
                        .build());

            } else {

                if (latestKill.getMirrorMonked()) {

                    String intendedTargetUsername = latestKill.getIntendedTarget().getUsername();
                    String msg = " tried to KILL "
                            + intendedTargetUsername
                            + " but was KILLED by "
                            + intendedTargetUsername
                            + "'s Mirror Monk";

                    components.add(TextComponent.builder()
                            .fontSize(2)
                            .value((isUserInCharge ? "You" : latestKill.getKiller().getUsername()) + msg)
                            .build());

                } else {

                    components.add(TextComponent.builder()
                            .fontSize(2)
                            .value((isUserInCharge ? "YOU" : latestKill.getKiller().getUsername())
                                    + " KILLED "
                                    + latestKill.getActuallyDead().getUsername())
                            .build());

                }


            }

            if (latestKill.getMartyred()) {

                MyUser actuallyDead = latestKill.getActuallyDead();
                MyUser intendedTarget = latestKill.getIntendedTarget();

                String msg = "";

                if (actuallyDead != null && actuallyDead.getUsername().equals(intendedTarget.getUsername()))
                    msg = actuallyDead.getUsername();
                else
                    msg = intendedTarget.getUsername();

                if (user.getUsername().equals(msg))
                    msg = "You";

                msg += " gained a random HONOR token for being a KILL target and holding MARTYR card";

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(msg)
                        .build());
            }

            if (isUserInCharge) {

                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "jbs-end");
                                        }})
                                        .style(
                                                ButtonComponentStyle.builder()
                                                        .title("Next")
                                                        .color("blue")
                                                        .build()
                                        )
                                        .build()
                        ))
                        .build());

            }


            map.put(user.getUsername(), components);

        }

    }

    // ************************ ACTIONS ************************

    /**
     * only applicable for blind and shinobi
     */
    public void play(Principal principal, NinjaCard ninjaCard) {

        MyUser user = userService.findByUsername(principal.getName());

//        if (lifeAndDeathService.isDead(user, true))
//            return;
        if (!(ninjaCard.getCategory().equals("blind") || ninjaCard.getCategory().equals("shinobi")))
            return;

//        if (ninjaCardService.tooLateToPlay(ninjaCard))
//            return; // can't play lesser order blind/shinobi card.

        Map map = new HashMap();
        map.put(user, ninjaCard);
        ninjaCardService.addToBeActivate(map);
        ninjaCardService.removeFromHand(user, ninjaCard);

    }

    public void shinobiSeeHouseCard() {
        stage = JudgeBlindShinobiStage.SHINOBI_SEE_HOUSE_CARD;
    }

    /**
     * judge or blind assassin.
     * should always send target.
     */
    public void kill(Map body) {

        KillIntention killIntention = KillIntention.builder().build();
        killIntention.setKiller(userService.findByUsername(selectorService.getSelecting()));
        killIntention.setIntendedTarget(selectorService.getSelected().size() > 0
                ? userService.findByUsername(selectorService.lastSelected())
                : null);

        /**
         * judge/blind/shinobi MUST have an intended target unless there's no one left to kill (exclude self)
         * */
        if (killIntention.getIntendedTarget() == null) {

            List<MyUser> stillAlives = lifeAndDeathService.findAllByIsDead(false, killIntention.getKiller());
            if (stillAlives.size() > 0) // there's someone else that can be KILLED.
                return;

        }


        Boolean shinobiKill = body.containsKey("kill") ? (boolean) body.get("kill") : null;

        if (shinobiKill != null) // source is from shinobi
            killIntention.setShinobiDecidedToKill(shinobiKill.booleanValue());

        NinjaCard source = ninjaCardService.findLastUsed().entrySet().stream().map(x -> x.getValue()).findFirst().get();

        lifeAndDeathService.kill(killIntention, source);

        stage = JudgeBlindShinobiStage.OUTCOME;

    }

    public void end(Principal principal) throws Exception {

        selectorService.getSelected().clear();

        if (activatedFromGrave) { // redirect back to trickster's page
            
            activatedFromGrave = false;
            tricksterService.resetFromGrave(principal);
            return;

        }

        if (ninjaCardService.getToBeActivated().isEmpty()) {

            if (judgeOrBlindOrShinobi == JudgeBlindShinobi.JUDGE) {
                resetForBlind();
                gameProgress.update(GameProgressData.BLIND);
            } else if (judgeOrBlindOrShinobi == JudgeBlindShinobi.BLIND) {
                resetForShinobi();
                gameProgress.update(GameProgressData.SHINOBI);
            } else
                gameProgress.update(GameProgressData.REVEAL);

        } else {

            Map<MyUser, NinjaCard> first = ninjaCardService.getToBeActivated().remove(0);
            var kvp = first.entrySet().stream().findFirst().get();
            var user = kvp.getKey();
            var ninjaCard = kvp.getValue();
            ninjaCardService.addToUsed(user, ninjaCard);
            selectorService.reset(user.getUsername());

            stage = JudgeBlindShinobiStage.CHOOSE_TARGET;

        }

    }


    public void setJudgeOrBlindOrShinobi(JudgeBlindShinobi judgeBlindShinobi) {
        this.judgeOrBlindOrShinobi = judgeBlindShinobi;
    }

    public void setActivatedFromGrave(boolean activatedFromGrave) {
        this.activatedFromGrave = activatedFromGrave;
    }


    public void resetForBlind() {

        stage = JudgeBlindShinobiStage.PLAY;
        judgeOrBlindOrShinobi = JudgeBlindShinobi.BLIND;

    }

    public void resetForShinobi() {

        stage = JudgeBlindShinobiStage.PLAY;
        judgeOrBlindOrShinobi = JudgeBlindShinobi.SHINOBI;

    }

    public void setStage(JudgeBlindShinobiStage stage) {
        this.stage = stage;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class KillIntention {

    private MyUser killer;
    private MyUser intendedTarget;

    @Builder.Default
    private Boolean fromJudge = false;
    private Boolean fromBlind = false;
    private Boolean fromShinobi = false;
    private Boolean shinobiDecidedToKill = false;

}