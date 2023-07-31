package com.honor;

import com.GameController;
import com.GameProgressService;
import com.MyUser;
import com.UserService;
import com.house.HouseService;
import com.props.*;
import com.reveal.OutcomeResult;
import com.reveal.RevealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AwardService {

    @Autowired
    UserService userService;

    @Autowired
    HonorService honorService;

    @Autowired
    RevealService revealService;

    @Autowired
    HouseService houseService;

    @Autowired
    private GameProgressService gameProgress;

    @Autowired
    @Lazy
    private GameController gameController;

    private boolean awardedHonorPoints;

    private UltimateWinner ultimateWinner = UltimateWinner.builder().build();

    public void reset() throws Exception {

        awardedHonorPoints = false;
        ultimateWinner = UltimateWinner.builder().build();

    }

    /**
     *
     */
    public Map getData() {

        if (ultimateWinner.getWinners().isEmpty())
            return getLeaderboard();
        else
            return getEndOfGame();
    }

    private Map getLeaderboard() {
        if (!awardedHonorPoints) {
            awardedHonorPoints = true;
            awardHonorPointsToVictor();
        }

        Map map = new LinkedHashMap();

        List<MyUser> all = userService.findAll();

        Collections.sort(all, (user1, user2) ->
                Integer.compare(honorService.findAllByUser(user2).size(),
                        honorService.findAllByUser(user1).size()));

        Map<MyUser, List<Integer>> userTokenMap = new HashMap<>();
        for (MyUser user : userService.findAll()) {
            List<HonorToken> honorTokens = honorService.findAllByUser(user);
            userTokenMap.put(user, Collections.nCopies(honorTokens.size(), 0));
        }

        Function<MyUser, List<Integer>> userToRealTokens = user -> honorService.findAllByUser(user).stream().map(x -> x.getValue()).collect(Collectors.toList());

        for (MyUser user : all) {

            List components = new ArrayList();
            components.add(TextComponent.builder()
                    .fontSize(4)
                    .value("Leaderboard")
                    .build());
            components.add(SingleReadOnlyListComponent
                    .builder()
                    .playerTokens(
                            all.stream().map(x -> PlayerTokenComponent.builder()
                                    .username(x.getUsername())
                                    .tokens(
                                            user.getUsername().equals(x.getUsername())
                                                    ? userToRealTokens.apply(user)
                                                    : userTokenMap.get(x)
                                    )
                                    .build()).collect(Collectors.toList())
                    ).build());


            if (user.isHost()) {
                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "endornextround");
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

    private Map getEndOfGame() {

        Map map = new LinkedHashMap();

        List<MyUser> all = userService.findAll();

//        Collections.sort(all, (user1, user2) ->
//                Integer.compare(honorService.findAllByUser(user2).size(),
//                        honorService.findAllByUser(user1).size()));

        Collections.sort(all, (user1, user2) ->
                Integer.compare(honorService.findAllByUser(user2).stream().mapToInt(x -> x.getValue()).sum(),
                        honorService.findAllByUser(user1).stream().mapToInt(x -> x.getValue()).sum()));

        Map<MyUser, List<Integer>> userTokenMap = new LinkedHashMap<>();

        all.forEach(x -> userTokenMap.put(x, honorService.findAllByUser(x)
                .stream().map(y -> y.getValue()).collect(Collectors.toList())));

        String title = String.join(", ", ultimateWinner.getWinners().stream().map(x -> x.getUsername())
                .collect(Collectors.toList()))
                + " won with a total of "
                + ultimateWinner.getHighscore()
                + " HONOR points!";

        for (MyUser user : userService.findAll()) {

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value(title).build());
            components.add(TextComponent.builder().fontSize(2).value("Leaderboard").build());

            components.add(SingleReadOnlyListComponent
                    .builder()
                    .playerTokens(
                            userTokenMap.entrySet().stream().map(x -> PlayerTokenComponent.builder()
                                    .username(x.getKey().getUsername())
                                    .tokens(x.getValue())
                                    .build()).collect(Collectors.toList())
                    ).build());

            if (user.isHost()) {
                components.add(MultiButtonComponents
                        .builder()
                        .numCols(1)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "startgame");
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .color("blue")
                                                .title("Play Again")
                                                .build())
                                        .build()
                        ))
                        .build());
            }

            map.put(user.getUsername(), components);

        }


        return map;

    }

    // **************************** ACTIONS ****************************

    private void awardHonorPointsToVictor() {

        OutcomeResult outcome = revealService.getOutcome();
        Set<MyUser> winners = outcome.getWinners();
        winners.forEach(x -> honorService.giveRandomHonorToken(x));

    }

    public void endOrNextRound() throws Exception {
        Map<MyUser, List<HonorToken>> all = honorService.findAll();

        int maxSum = Integer.MIN_VALUE;

        for (Map.Entry<MyUser, List<HonorToken>> entry : all.entrySet()) {
            MyUser user = entry.getKey();
            List<HonorToken> honorTokens = entry.getValue();

            int sum = honorTokens.stream().mapToInt(x -> x.getValue()).sum();

            if (sum > maxSum) {
                maxSum = sum;
            }
        }

        if (maxSum >= 10) {
            int finalMaxSum = maxSum;

            List<MyUser> highestScoringPlayer = honorService.findAll().entrySet().stream()
                    .filter(x -> x.getValue().stream().mapToInt(y -> y.getValue()).sum() == finalMaxSum)
                    .map(j -> j.getKey()).collect(Collectors.toList());

            ultimateWinner.setWinners(highestScoringPlayer.stream().collect(Collectors.toSet()));
            ultimateWinner.setHighscore(finalMaxSum);

        } else {

            gameController.nextRound();

        }

    }


}

