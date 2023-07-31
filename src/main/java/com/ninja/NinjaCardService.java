package com.ninja;

import com.GameProgressData;
import com.GameProgressService;
import com.MyUser;
import com.UserService;
import com.props.MultiComponents;
import com.props.MultiComponentsStyle;
import com.props.NinjaCardComponent;
import com.props.NinjaCardComponentStyle;
import com.spymystic.SpyMysticService;
import com.spymystic.SpyOrMystic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class NinjaCardService {

    final List<NinjaCard> refNinjaCards = new ArrayList<>();
    final List<NinjaCard> ninjaCards = new ArrayList<>();
    final Map<MyUser, List<NinjaCard>> dealtMap = new LinkedHashMap(); // users are dealt these ninja cards, but not on hand
    final Map<MyUser, List<NinjaCard>> onHandMap = new LinkedHashMap(); // users have this on hand and can see them
    final Map<MyUser, List<NinjaCard>> onHoldMap = new LinkedHashMap(); // users have these, but can't see them.
    final List<NinjaCard> discards = new ArrayList<>(); // what goes into to discard pile after drafting phase?
    final List<Map<MyUser, NinjaCard>> used = new ArrayList<>(); // what cards were used and cannot be graved?
    private List<Map<MyUser, NinjaCard>> toBeActivated = new ArrayList<>();

    @Autowired
    private UserService userService;

    @Autowired
    private GameProgressService gameProgress;

    @Autowired
    @Lazy
    private SpyMysticService spyMysticService;

    private Random random = new SecureRandom();

    public void reset() throws Exception {
        if (refNinjaCards.isEmpty()) {

            for (int i = 1; i <= 6; i++)
                refNinjaCards.add(NinjaCard.builder().category("spy").code("spy").executionOrder(i).build());
            for (int i = 1; i <= 6; i++)
                refNinjaCards.add(NinjaCard.builder().category("mystic").code("mystic").executionOrder(i).build());
            for (int i = 1; i <= 6; i++)
                refNinjaCards.add(NinjaCard.builder().category("blind").code("blind").executionOrder(i).build());
            for (int i = 1; i <= 6; i++)
                refNinjaCards.add(NinjaCard.builder().category("shinobi").code("shinobi").executionOrder(i).build());

            refNinjaCards.add(NinjaCard.builder().category("react").code("monk").executionOrder(-1).build());
            refNinjaCards.add(NinjaCard.builder().category("react").code("martyr").executionOrder(-1).build());
            refNinjaCards.add(NinjaCard.builder().category("react").code("mastermind").executionOrder(-1).build());

            refNinjaCards.add(NinjaCard.builder().category("trickster").code("shape").executionOrder(1).build());
            refNinjaCards.add(NinjaCard.builder().category("trickster").code("grave").executionOrder(2).build());
            refNinjaCards.add(NinjaCard.builder().category("trickster").code("trouble").executionOrder(3).build());
            refNinjaCards.add(NinjaCard.builder().category("trickster").code("spirit").executionOrder(4).build());
            refNinjaCards.add(NinjaCard.builder().category("trickster").code("thief").executionOrder(5).build());
            refNinjaCards.add(NinjaCard.builder().category("trickster").code("judge").executionOrder(6).build());
        }

        ninjaCards.clear();
        ninjaCards.addAll(refNinjaCards);
        Collections.shuffle(ninjaCards, random);

        List<MyUser> all = userService.findAll();

        discards.clear();
        used.clear();
        toBeActivated.clear();

        List<NinjaCard> cards = new ArrayList<>(refNinjaCards);
        Collections.shuffle(cards, random);

        for (int i = 0; i < all.size(); i++) {
            MyUser user = all.get(i);

            dealtMap.put(user, new ArrayList<>());
            onHandMap.put(user, new ArrayList<>());
            onHoldMap.put(user, new ArrayList<>());

            List<NinjaCard> temp = new ArrayList<>();
            temp.addAll(List.of(cards.remove(0), cards.remove(0), cards.remove(0)));
            dealtMap.put(user, temp);

        }


    }

    public void addToHand(MyUser user, NinjaCard ninjaCard) {

        List<NinjaCard> cards = onHandMap.get(user);

        boolean anyMatch = cards.stream().anyMatch(x -> x.getCategory().equals(ninjaCard.getCategory())
                && x.getCode().equals(ninjaCard.getCode())
                && x.getExecutionOrder() == ninjaCard.getExecutionOrder());

        if (anyMatch)
            return;

        cards.add(ninjaCard);
    }

    public void removeFromDealt(MyUser user, NinjaCard ninjaCard) {
        dealtMap.get(user).remove(ninjaCard);
    }

    public void clearDealt(MyUser user) {
        dealtMap.get(user).clear();
    }

    public void addToOnHold(MyUser user, NinjaCard... ninjaCards) {
        for (NinjaCard ninjaCard : ninjaCards)
            onHoldMap.get(user).add(ninjaCard);
    }

    public void moveAllOnHoldToOnHand() {

        onHoldMap.forEach((user, ninjaCards) -> {
            dealtMap.get(user).addAll(ninjaCards);
            onHoldMap.get(user).clear();
        });

    }

    public void moveAllOnHoldToDiscarded() {
        onHoldMap.forEach((user, ninjaCards) -> {
            discards.addAll(ninjaCards);
        });
    }

    public List<NinjaCard> allOnHand(MyUser user) {
        return onHandMap.get(user);
    }

    public void removeFromHand(MyUser user, NinjaCard ninjaCard) {
        boolean removed = onHandMap.get(user).remove(ninjaCard);
    }

    public void addToUsed(MyUser user, NinjaCard ninjaCard) {
        Map map = new HashMap();
        map.put(user, ninjaCard);

//        boolean anyMatch = used.stream().map(x -> x.get(user)).anyMatch(x -> x.getCategory().equals(ninjaCard.getCategory())
//                && x.getCode().equals(ninjaCard.getCode())
//                && x.getExecutionOrder() == ninjaCard.getExecutionOrder());

        boolean anyMatch = used.stream().map(x -> x.get(user)).anyMatch(x -> x == ninjaCard);

        if (anyMatch)
            return;

        used.add(map);
    }

    public MultiComponents allUsedMultiComponents() {

        final List<NinjaCardComponent> ninjaCardComponents = new ArrayList<>();
        for (Map<MyUser, NinjaCard> map : used) {
            var get = map.entrySet().stream().findFirst().get();
            MyUser key = get.getKey();
            NinjaCard value = get.getValue();

            ninjaCardComponents.add(
                    NinjaCardComponent
                            .builder()
                            .owner(key.getUsername())
                            .ninjaCard(value)
                            .style(NinjaCardComponentStyle.builder().minified(true).build())
                            .build()
            );
        }

        return MultiComponents.builder()
                .ninjaCardComponents(ninjaCardComponents)
                .title("Used:")
                .style(MultiComponentsStyle.builder()
                        .border("rounded-lg border border-slate-500 border-dashed")
                        .build())
                .numCols(5)
                .build();

    }

    public List<NinjaCard> getDiscards() {
        return discards;
    }

    public NinjaCard findFromDiscards(String category, String code, int executionOrder) {

        NinjaCard found = null;

        for (NinjaCard discard : discards) {
            if (discard.getCategory().equals(category) && discard.getCode().equals(code) && discard.getExecutionOrder() == executionOrder) {
                found = discard;
                break;
            }
        }
        return found;
    }

    public void removefromDiscards(NinjaCard discard) {
        discards.remove(discard);
    }

    public Map<MyUser, List<NinjaCard>> dealtMap() {
        return dealtMap;
    }

    public Map<MyUser, List<NinjaCard>> onHandMap() {
        return onHandMap;
    }

    public List<Map<MyUser, NinjaCard>> getToBeActivated() {
        return toBeActivated;
    }

    public Map<MyUser, NinjaCard> removeFirstFromGetToBeActivated() {
        return toBeActivated.remove(0);
    }

    public void removeAllToBeActivated(MyUser victim) {
        toBeActivated.removeIf(x -> x.containsKey(victim));
    }

    public MultiComponents getToBeActivatedNinjaCardsComponents() {

        List<NinjaCardComponent> others = new ArrayList<>();

        toBeActivated.forEach(x -> x.forEach((k, v) -> {
            var component = NinjaCardComponent
                    .builder()
                    .style(NinjaCardComponentStyle.builder()
                            .minified(true)
                            .build())
                    .owner(k.getUsername())
                    .ninjaCard(v)
                    .build();

            others.add(component);
        }));

        return MultiComponents.builder()
                .title("Pending Activation:")
                .style(MultiComponentsStyle.builder()
                        .border("rounded-lg border mb-4 ")
                        .build())
                .numCols(5)
                .ninjaCardComponents(others)
                .build();

    }

    public void addToBeActivate(Map<MyUser, NinjaCard> map) {

        Map.Entry<MyUser, NinjaCard> get = map.entrySet().stream().findFirst().get();
        MyUser user = get.getKey();
        NinjaCard ninjaCard = get.getValue();

        for (Map<MyUser, NinjaCard> cardMap : toBeActivated) {

            NinjaCard card = cardMap.get(user);
            if (card == null)
                continue;

            if (card.getCategory().equals(ninjaCard.getCategory())
                    && card.getCode().equals(ninjaCard.getCode())
                    && card.getExecutionOrder() == ninjaCard.getExecutionOrder()) {
                return;
            }
        }

        toBeActivated.add(map);

        Collections.sort(toBeActivated, Comparator.comparing(entry -> {
            NinjaCard next = entry.values().iterator().next();
            return next.getExecutionOrder();
        }));
    }

    public void addUsedAndActivatedCardsAtFirst(Map res) {

        var used = allUsedMultiComponents();
        var components = getToBeActivatedNinjaCardsComponents();

        res.forEach((o, o2) -> {
            var list = (List) o2;

            list.add(0, components);
            list.add(0, used);
        });

    }

    public boolean tooLateToPlay(NinjaCard ninjaCard) {

        if (ninjaCard.getExecutionOrder() > 0) {

            GameProgressData get = gameProgress.get();

            String compareCategory = null;

            if (get == GameProgressData.SPY_MYSTIC) {
                SpyOrMystic spyOrMystic = spyMysticService.getSpyOrMystic();
                compareCategory = spyOrMystic == SpyOrMystic.SPY ? "spy" : "mystic";

            } else if (get == GameProgressData.TRICKSTER) {
                compareCategory = "trickster";
            } else if (get == GameProgressData.BLIND) {
                compareCategory = "blind";
            } else if (get == GameProgressData.SHINOBI) {
                compareCategory = "shinobi";
            }

            String finalCompareCategory = compareCategory;

            return used.stream().anyMatch(x -> {
                var nc = x.values().stream().findFirst().get();
                return nc.getCategory().equals(finalCompareCategory)
                        && nc.getExecutionOrder() > ninjaCard.getExecutionOrder();
            });

        }

        return false;
    }

    public Map<MyUser, NinjaCard> findLastUsed() {
        return used.get(used.size() - 1);
    }

    public void removeAllOnHand(MyUser user) {
        onHandMap.get(user).clear();
    }

    public void debugMode() {

        List<NinjaCard> temp = new ArrayList<>(refNinjaCards);
        Collections.shuffle(temp, random);

        onHandMap.clear();
        discards.clear();

        List<MyUser> all = userService.findAll();

        for (int i = 0; i < all.size(); i++) {

            MyUser user = all.get(i);

            if (i == 0) {

                List<NinjaCard> cards = new ArrayList<>();

                for (int j = 0; j < 2; j++) {
                    NinjaCard card = null;

                    if (j == 0)
                        card = temp.stream().filter(x -> x.getCode().equals("grave")).findFirst().get();
                    else if (j == 1)
                        card = temp.get(0);

                    cards.add(card);
                    temp.remove(card);
                }

                onHandMap.put(user, cards);

            } else {

                List<NinjaCard> cards = new ArrayList<>();

                while (cards.size() < 2) {

                    NinjaCard c = temp.remove(0);

                    if (!c.getCategory().equals("trickster"))
                        continue;

                    cards.add(c);

                }

                onHandMap.put(user, cards);

            }


        }

        discards.add(refNinjaCards.stream().filter(x -> x.getCode().equals("judge")).findFirst().get());
        discards.add(refNinjaCards.stream().filter(x -> x.getCode().equals("mystic")).findFirst().get());


    }
}
