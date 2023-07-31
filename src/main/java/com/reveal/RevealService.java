package com.reveal;

import com.MyUser;
import com.UserService;
import com.honor.HonorService;
import com.house.HouseService;
import com.lifeanddeath.LifeAndDeathService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

enum RevealStage {

    CHECK_IF_MASTERMIND_WINS

}

@Service
public class RevealService {

    @Autowired
    UserService userService;

    @Autowired
    LifeAndDeathService lifeAndDeathService;

    @Autowired
    HouseService houseService;

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    HonorService honorService;

    OutcomeResult outcome = OutcomeResult.builder().build();

    public void reset() {
        outcome = OutcomeResult.builder().build();
    }

    public Map getData() throws Exception {

        if (!outcome.getDecided()) {
            outcome.setDecided(true);
            determineWinner();
        }

        Map map = new LinkedHashMap();

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();

            String title = "";
            String subtitle = null;

            if (outcome.getWinOutcome() == WinOutcome.DRAW)
                title = "DRAW!";
            else if (outcome.getWinOutcome() == WinOutcome.LOTUS_WINS)
                title = "LOTUS WINS!";
            else if (outcome.getWinOutcome() == WinOutcome.CRANE_WINS)
                title = "CRANE WINS!";
            else if (outcome.getWinOutcome() == WinOutcome.NOBODY_WINS)
                title = "NOBODY WINS!";

            components.add(TextComponent.builder()
                    .fontSize(4)
                    .value(title)
                    .build());

            if (outcome.getMastermindUser() != null) {

                int loyalty = houseService.findByUser(outcome.getMastermindUser()).getLoyalty();

                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(outcome.getMastermindUser().getUsername()
                                + " (" + (loyalty == 0 ? "LOTUS" : loyalty == 1 ? "CRANE" : "RONIN") + ") "
                                + " survived and wins with MASTERMIND")
                        .build());

            }

            // regular victory
            if (outcome.getRoninSurvived()) {
                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(outcome.getRonin().getUsername()
                                + " (RONIN) " + "survived and won as well")
                        .build());

            }


            Function<Integer, RevealComponent> loyaltyToRevealComponent = x -> {

                List<MyUser> all = houseService.findUsersByLoyalty(x);

                // for each all, generate a list of RevealPlayerComponent
                Function<MyUser, RevealPlayerComponent> userToRevealPlayerComponent = y -> RevealPlayerComponent.builder()
                        .rank(houseService.findByUser(y).getRank())
                        .username(y.getUsername())
                        .dead(lifeAndDeathService.isDead(y, true))
                        .build();

                List<RevealPlayerComponent> revealPlayerComponents = all.stream().map(userToRevealPlayerComponent).collect(Collectors.toList());

                revealPlayerComponents.sort(Comparator.comparingInt(RevealPlayerComponent::getRank));

                return RevealComponent
                        .builder()
                        .loyalty(x)
                        .players(revealPlayerComponents)
                        .build();

            };

            components.add(MultiRevealComponents.builder()
                    .revealComponents(List.of(
                            loyaltyToRevealComponent.apply(0),
                            loyaltyToRevealComponent.apply(1)
                    ))
                    .build());

            components.add(MultiRevealComponents.builder()
                    .revealComponents(List.of(
                            loyaltyToRevealComponent.apply(2)
                    ))
                    .build());

            if (user.isHost()) {

                components.add(MultiButtonComponents.builder()
                        .numCols(1)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap<>() {{
                                            put("id", "reveal-awardtoken");
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build());

            }


            map.put(user.getUsername(), components);

        }

        return map;

    }

    // ************************* ACTIONS *************************

    public void determineWinner() {

        Set<MyUser> individualWinners = new LinkedHashSet<>();

        List<MyUser> stillAlives = lifeAndDeathService.findAllByIsDead(false);
        MyUser userWithMastermind = null;

        for (MyUser user : stillAlives) {

            Map<MyUser, List<NinjaCard>> onHandMap = ninjaCardService.onHandMap();

            List<NinjaCard> ninjaCards = onHandMap.get(user);

            if (ninjaCards.stream().anyMatch(x -> x.getCode().equals("mastermind"))) {
                userWithMastermind = user;
                break;
            }
        }

        if (userWithMastermind == null) { // normal scenario

            List<MyUser> lotus = houseService.findUsersByLoyalty(0);
            List<MyUser> crane = houseService.findUsersByLoyalty(1);

            Collections.sort(lotus, Comparator.comparingInt(x -> houseService.findByUser(x).getRank()));
            Collections.sort(crane, Comparator.comparingInt(x -> houseService.findByUser(x).getRank()));

            for (int i = 0; i < lotus.size(); i++) {

                boolean lotusDied = lifeAndDeathService.isDead(lotus.get(i), true);
                boolean craneDied = lifeAndDeathService.isDead(crane.get(i), true);

                if (lotusDied && !craneDied) {
                    individualWinners.addAll(crane);
                    outcome.setWinOutcome(WinOutcome.CRANE_WINS);
                } else if (!lotusDied && craneDied) {
                    individualWinners.addAll(lotus);
                    outcome.setWinOutcome(WinOutcome.LOTUS_WINS);
                }

                if (individualWinners.size() > 0)
                    break;

            }

            if (individualWinners.isEmpty()) {
                individualWinners.addAll(stillAlives);
                outcome.setWinOutcome(WinOutcome.DRAW);
            }

        } else {

            // win with mastermind
            outcome.setMastermindUser(userWithMastermind);

            int loyalty = houseService.findByUser(userWithMastermind).getLoyalty();

            if (loyalty == 0)
                outcome.setWinOutcome(WinOutcome.LOTUS_WINS);
            else if (loyalty == 1)
                outcome.setWinOutcome(WinOutcome.CRANE_WINS);
            else if (loyalty == 2)
                outcome.setWinOutcome(WinOutcome.NOBODY_WINS);

            individualWinners.addAll(houseService.findUsersByLoyalty(loyalty));

        }

        MyUser ronin = houseService.findUsersByLoyalty(2).stream().findFirst().orElse(null);
        if (ronin != null && stillAlives.contains(ronin)) {
            individualWinners.add(ronin);
            outcome.setRoninSurvived(true);
            outcome.setRonin(ronin);
        }

        outcome.setWinners(individualWinners);


    }

    public OutcomeResult getOutcome() {
        return outcome;
    }

}
