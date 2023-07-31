package com.trickster;

import com.GameProgressService;
import com.MyUser;
import com.SelectorService;
import com.UserService;
import com.honor.HonorService;
import com.honor.HonorToken;
import com.house.HouseService;
import com.ninja.NinjaCardService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum TricksterSpiritStage {
    PICK_A_PLAYER,
    SEE_HOUSE_OR_HONOR,
    INSUFFICIENT_HONOR_TO_SWAP,
    SEE_HOUSE,
    SEE_ONE_HONOR,
    CHOOSE_HONOR_TO_SWAP,
    SWAPPED_HONOR

}

@Service
public class TricksterSpiritService {

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
    TricksterSpiritStage stage = TricksterSpiritStage.PICK_A_PLAYER;

    @Autowired
    private SelectorService selectorService;
    @Autowired
    private HonorService honorService;
    private boolean seeHouseOrHonor;
    private int visibleTargetHonorTokenIndex = -1; // which honorToken did selecting chose to SEE?
    private int selectingSelfHonorTokenIndexToSwap = -1; // which honorToken did selecting chose to GIVE?
    private int selectedSelfHonorTokenIndexToSwap = -1; // which honorToken did selecting chose to TAKE?
    private boolean swapped = false;

    public void reset() {
        stage = TricksterSpiritStage.PICK_A_PLAYER;
        seeHouseOrHonor = false;
        visibleTargetHonorTokenIndex = -1;
        selectingSelfHonorTokenIndexToSwap = -1;
        selectedSelfHonorTokenIndexToSwap = -1;
        swapped = false;
    }

    public void getData(Map map) throws Exception {

        if (stage == TricksterSpiritStage.PICK_A_PLAYER)
            getPickAPlayer(map);
        else if (stage == TricksterSpiritStage.SEE_HOUSE_OR_HONOR)
            getSeeHouseOrHonor(map);
        else if (stage == TricksterSpiritStage.SEE_HOUSE)
            getSeeHouse(map);
        else if (stage == TricksterSpiritStage.INSUFFICIENT_HONOR_TO_SWAP)
            getInsufficientHonorToSwap(map);
        else if (stage == TricksterSpiritStage.SEE_ONE_HONOR)
            getSeeOneHonor(map);
        else if (stage == TricksterSpiritStage.CHOOSE_HONOR_TO_SWAP)
            getChooseHonorToSwap(map);
        else if (stage == TricksterSpiritStage.SWAPPED_HONOR)
            getSwappedHonorToken(map);

    }

    private void getPickAPlayer(Map map) throws Exception {

        MyUser userInCharge = userService.findByUsername(selectorService.getSelecting());

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());

            if (isInCharge) {
                components.add(TextComponent.builder().fontSize(2)
                        .value("Look at another player's HOUSE card or HONOR Token")
                        .build());
                components.add(TextComponent.builder().fontSize(2)
                        .value("You may swap one HONOR token")
                        .build());
            } else {
                components.add(TextComponent.builder().fontSize(2)
                        .value(selectorService.getSelecting() + " is thinking...")
                        .build());
            }

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
                                            put("id", "spirit-confirmselection");
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

    private void getSeeHouseOrHonor(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? "Look at " + selectorService.lastSelected() + "'s HOUSE or HONOR token?"
                                    : selectorService.getSelecting() + " is deciding whether to look at " + selectorService.lastSelected() + "'s HOUSE or HONOR token..."
                    )
                    .build());

            if (isInCharge) {
                components.add(MultiButtonComponents
                        .builder()
                        .numCols(2)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "spirit-lookathouseorhonor");
                                            put("seeHouseOrHonor", true);
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .title("HOUSE")
                                                .color("green")
                                                .build())
                                        .build(),
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "spirit-lookathouseorhonor");
                                            put("seeHouseOrHonor", false);
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .title("HONOR")
                                                .color("orange")
                                                .build())
                                        .build()
                        ))
                        .build()
                );

            }

            map.put(user.getUsername(), components);

        }

    }

    private void getSeeHouse(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2)
                    .value(
                            isInCharge
                                    ? selectorService.getSelected().get(0) + "'s HOUSE card is..."
                                    : selectorService.getSelecting() + " saw " + selectorService.getSelected().get(0) + "'s HOUSE card"
                    )
                    .build());

            if (isInCharge) {

                components.add(MultiComponents.builder()
                        .numCols(1)
                        .houseCardComponents(Arrays.asList(
                                HouseCardComponent.builder()
                                        .houseCard(houseService.findByUser(userService.findByUsername(selectorService.getSelected().get(0))))
                                        .build()
                        ))
                        .build());

                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "spirit-sawhousecard");
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build()
                );

            }

            map.put(user.getUsername(), components);

        }

    }

    private void getInsufficientHonorToSwap(Map map) throws Exception {

        // you do not have any HONOR tokens to swap.
        // selected.get(0) do not have any HONOR tokens to swap.

        List<String> insufficientUsernames = new ArrayList<>();

        if (honorService.getNumHonor(userService.findByUsername(selectorService.getSelecting())) == 0)
            insufficientUsernames.add(selectorService.getSelecting());
        if (honorService.getNumHonor(userService.findByUsername(selectorService.getSelected().get(0))) == 0)
            insufficientUsernames.add(selectorService.getSelected().get(0));

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());

            if (isInCharge) {

                if (insufficientUsernames.size() == 2) {
                    components.add(TextComponent.builder().fontSize(2).value(
                                    "Can't swap HONOR tokens because both you and " + selectorService.getSelected().get(0) + " have none")
                            .build());
                } else {

                    if (insufficientUsernames.contains(selectorService.getSelecting())) {
                        components.add(TextComponent.builder().fontSize(2).value(
                                        "Can't swap because you have no HONOR tokens")
                                .build());
                    } else if (insufficientUsernames.contains(selectorService.getSelected().get(0))) {
                        components.add(TextComponent.builder().fontSize(2).value(
                                        "Can't swap because " + selectorService.getSelected().get(0) + " have no HONOR tokens")
                                .build());
                    }
                }

            } else {

                if (insufficientUsernames.size() == 2) {
                    components.add(TextComponent.builder().fontSize(2).value(
                                    "Can't swap because both "
                                            + String.join(" and ", insufficientUsernames)
                                            + " have no HONOR tokens")
                            .build());
                } else {

                    if (insufficientUsernames.contains(selectorService.getSelecting())) {
                        components.add(TextComponent.builder().fontSize(2).value(
                                        "Can't swap because " + selectorService.getSelecting() + " have no HONOR tokens")
                                .build());
                    } else if (insufficientUsernames.contains(selectorService.getSelected().get(0))) {
                        components.add(TextComponent.builder().fontSize(2).value(
                                        "Can't swap because " + selectorService.getSelected().get(0) + " have no HONOR tokens")
                                .build());
                    }
                }
            }

            if (isInCharge) {
                components.add(MultiButtonComponents
                        .builder()
                        .numCols(1)
                        .buttonComponents(Arrays.asList(
                                ButtonComponent.builder()
                                        .body(new HashMap() {{
                                            put("id", "spirit-end");
                                        }})
                                        .style(ButtonComponentStyle.builder()
                                                .title("Next")
                                                .color("blue")
                                                .build())
                                        .build()
                        ))
                        .build()
                );

            }

            map.put(user.getUsername(), components);

        }

    }

    private void getSeeOneHonor(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2).value(
                            isInCharge ?
                                    "Look at ONE " + selectorService.getSelected().get(0) + "'s HONOR token"
                                    : selectorService.getSelecting() + "is choosing to look at ONE " + selectorService.getSelected().get(0) + "'s HONOR token")
                    .build());

            if (isInCharge) {

                List<HonorToken> all = honorService.findAllByUser(userService.findByUsername(selectorService.getSelected().get(0)));
                List<HonorTokenComponent> honorTokenComponents = IntStream.range(0, all.size())
                        .mapToObj(i -> HonorTokenComponent
                                .builder()
                                .honorToken(
                                        HonorToken.builder().build() // show selecting "?"s since he can't see them
                                )
                                .body(new HashMap() {{
                                    put("id", "spirit-lookathonortoken");
                                    put("index", i);
                                }})
                                .build())
                        .collect(Collectors.toList());

                components.add(
                        MultiComponents
                                .builder()
                                .numCols(5)
                                .honorTokenComponents(honorTokenComponents)
                                .build()
                );

            }

            map.put(user.getUsername(), components);

        }

    }

    private void getChooseHonorToSwap(Map map) throws Exception {

        // show all of yours
        // show all of theirs

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());
            components.add(TextComponent.builder().fontSize(2).value(
                            isInCharge ?
                                    "Choose ONE HONOR token from yours and " + selectorService.getSelected().get(0) + "'s to swap, or don't"
                                    : selectorService.getSelecting() + " is deciding which " + selectorService.getSelected().get(0) + "'s HONOR token to look at")
                    .build());

            if (isInCharge) {

                // show all of selecting's honor token
                components.add(TextComponent.builder()
                        .fontSize(2).value("Your HONOR Tokens:")
                        .build());

                List<HonorToken> allSelecting = honorService.findAllByUser(userService.findByUsername(selectorService.getSelecting()));
                List<HonorTokenComponent> selectingHonorTokenComponents = IntStream.range(0, allSelecting.size())
                        .mapToObj(i -> HonorTokenComponent
                                .builder()
                                .honorToken(allSelecting.get(i))
                                .highlight(selectingSelfHonorTokenIndexToSwap)
                                .body(new HashMap() {{
                                    put("id", "spirit-choosehonortokentoswap");
                                    put("index", i);
                                    put("self", true); // swap selecting's honor token for selected
                                }})
                                .build())
                        .collect(Collectors.toList());

                components.add(
                        MultiComponents
                                .builder()
                                .numCols(5)
                                .honorTokenComponents(selectingHonorTokenComponents)
                                .build()
                );

                // show all of selected's honor token
                components.add(TextComponent.builder()
                        .fontSize(2).value(selectorService.getSelected().get(0) + "'s HONOR Tokens:")
                        .build());

                List<HonorToken> allSelected = honorService.findAllByUser(userService.findByUsername(selectorService.getSelected().get(0)));
                List<HonorTokenComponent> selectedHonorTokenComponents = IntStream.range(0, allSelected.size())
                        .mapToObj(i -> HonorTokenComponent
                                .builder()
                                .honorToken(
                                        visibleTargetHonorTokenIndex == i
                                                ? allSelected.get(i)
                                                : HonorToken.builder().build() // others remain invisible
                                )
                                .highlight(selectedSelfHonorTokenIndexToSwap)
                                .body(new HashMap() {{
                                    put("id", "spirit-choosehonortokentoswap");
                                    put("index", i);
                                    put("self", false); // swap selected's honor token for selecting
                                }})
                                .build())
                        .collect(Collectors.toList());

                components.add(
                        MultiComponents
                                .builder()
                                .numCols(5)
                                .honorTokenComponents(selectedHonorTokenComponents)
                                .build()
                );

                components.add(MultiButtonComponents
                        .builder()
                        .numCols(2)
                        .buttonComponents(
                                Arrays.asList(
                                        ButtonComponent.builder()
                                                .body(new HashMap() {{
                                                    put("id", "spirit-swap");
                                                    put("swap", true);
                                                }})
                                                .style(ButtonComponentStyle.builder()
                                                        .title("Swap")
                                                        .color("green")
                                                        .build())
                                                .build(),
                                        ButtonComponent.builder()
                                                .body(new HashMap() {{
                                                    put("id", "spirit-swap");
                                                    put("swap", false);
                                                }})
                                                .style(ButtonComponentStyle.builder()
                                                        .title("Don't Swap")
                                                        .color("red")
                                                        .build())
                                                .build()
                                )
                        )
                        .build());


            }

            map.put(user.getUsername(), components);

        }

    }

    private void getSwappedHonorToken(Map map) throws Exception {

        for (MyUser user : userService.findAll()) {

            boolean isInCharge = user.getUsername().equalsIgnoreCase(selectorService.getSelecting());

            List components = new ArrayList();
            components.add(TextComponent.builder().fontSize(4).value("Spirit Merchant (Trickster)").build());

            if (isInCharge) {

                if (swapped) {

                    long totalTokens = honorService.findAllByUser(user).stream().count();
                    int totalTokensSummed = honorService.findAllByUser(user).stream().mapToInt(x -> x.getValue()).sum();

                    components.add(TextComponent.builder()
                            .fontSize(2)
                            .value("Your HONOR tokens' total sum is now " + totalTokensSummed)
                            .build());

                } else {

                    components.add(TextComponent.builder()
                            .fontSize(2)
                            .value("You did not swap HONOR tokens with " + selectorService.getSelected().get(0))
                            .build());

                }

                components.add(MultiButtonComponents
                        .builder()
                        .buttonComponents(
                                Arrays.asList(
                                        ButtonComponent.builder()
                                                .body(
                                                        new HashMap() {{
                                                            put("id", "spirit-end");
                                                        }}
                                                )
                                                .style(ButtonComponentStyle.builder()
                                                        .title("Next")
                                                        .color("blue")
                                                        .build())
                                                .build()
                                )
                        )
                        .build());


            } else {
                components.add(TextComponent.builder()
                        .fontSize(2)
                        .value(
                                swapped ?
                                        selectorService.getSelected().get(0).equalsIgnoreCase(user.getUsername())
                                                ? selectorService.getSelecting() + " swapped an HONOR token with you. You now have " + honorService.findSumOfTokens(user)
                                                : " swapped an HONOR token with " + selectorService.getSelected().get(0)
                                        :
                                        selectorService.getSelecting() + " did not swap HONOR tokens with " + selectorService.getSelected().get(0))
                        .build());
            }

            map.put(user.getUsername(), components);

        }

    }

    // ========================================== ACTIONS ==========================================


    public void confirmSelection() {

        if (selectorService.getSelected().size() != 1)
            return;

        stage = TricksterSpiritStage.SEE_HOUSE_OR_HONOR;
    }

    public void lookAtHouseOrHonor(Map body) {

        seeHouseOrHonor = (boolean) body.get("seeHouseOrHonor");

        if (seeHouseOrHonor) {

            stage = TricksterSpiritStage.SEE_HOUSE;

        } else {

//            honorService.giveRandomHonorToken(userService.findByUsername(selecting));
//            honorService.giveRandomHonorToken(userService.findByUsername(selected.get(0)));

            int a = honorService.getNumHonor(userService.findByUsername(selectorService.getSelected().get(0)));
            int b = honorService.getNumHonor(userService.findByUsername(selectorService.getSelecting()));

            if (a == 0 || b == 0)
                stage = TricksterSpiritStage.INSUFFICIENT_HONOR_TO_SWAP;
            else
                stage = TricksterSpiritStage.SEE_ONE_HONOR;

        }
    }

    public void sawHouseCard(Map body) {


        int a = honorService.getNumHonor(userService.findByUsername(selectorService.getSelecting()));
        int b = honorService.getNumHonor(userService.findByUsername(selectorService.getSelected().get(0)));

        if (a == 0 || b == 0)
            stage = TricksterSpiritStage.INSUFFICIENT_HONOR_TO_SWAP;
        else
            stage = TricksterSpiritStage.SEE_ONE_HONOR;

    }

    public void lookAtHonorToken(Map body) {
        visibleTargetHonorTokenIndex = (int) body.get("index");
        stage = TricksterSpiritStage.CHOOSE_HONOR_TO_SWAP;
    }


    public void chooseHonorTokenToSwap(Map body) {

        boolean self = (boolean) body.get("self");

        if (self)
            selectingSelfHonorTokenIndexToSwap = (int) body.get("index");
        else
            selectedSelfHonorTokenIndexToSwap = (int) body.get("index");

    }

    public void swap(Map body) {

        swapped = (boolean) body.get("swap");

        if (swapped) {

            if (selectingSelfHonorTokenIndexToSwap == -1 || selectedSelfHonorTokenIndexToSwap == -1)
                return;

            honorService.swap(
                    userService.findByUsername(selectorService.getSelecting()),
                    selectingSelfHonorTokenIndexToSwap,
                    userService.findByUsername(selectorService.getSelected().get(0)),
                    selectedSelfHonorTokenIndexToSwap
            );

        }

        stage = TricksterSpiritStage.SWAPPED_HONOR;

    }

    public void end(Principal principal) throws Exception {

        tricksterService.setStage(TricksterStage.PLAY_CARDS);
        tricksterService.end(principal);

    }


    public void setStage(TricksterSpiritStage stage) {
        this.stage = stage;
    }
}
