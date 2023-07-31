package com.house;

import com.GameProgressData;
import com.GameProgressService;
import com.MyUser;
import com.UserService;
import com.props.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HouseService {

    @Autowired
    UserService userService;

    // user gets what role
    Map<MyUser, HouseCard> userHouseMap = new HashMap<>();

    Random random = new SecureRandom();

    Set<MyUser> userSeenOwnHouseCard = new HashSet<>();

    @Autowired
    GameProgressService gameProgress;

    public void reset() {

        userHouseMap.clear();

        List<MyUser> all = userService.findAll();

        boolean isEven = all.size() % 2 == 0;
        int d = all.size() / 2;

        Collections.shuffle(all, random);

        for (int i = 0; i < d; i++)
            userHouseMap.put(all.remove(0), HouseCard.builder().loyalty(0).rank(i + 1).build());

        for (int i = 0; i < d; i++)
            userHouseMap.put(all.remove(0), HouseCard.builder().loyalty(1).rank(i + 1).build());

        if (!isEven)
            userHouseMap.put(all.remove(0), HouseCard.builder().loyalty(2).rank(-1).build());

        userSeenOwnHouseCard.clear();
    }

    public Map getHouseData() {

        Map res = new HashMap();

        for (MyUser user : userService.findAll()) {

            if (userSeenOwnHouseCard.contains(user)) {

                List<MyUser> waitingFor = userService.findAll();
                waitingFor.removeAll(userSeenOwnHouseCard);

                List components = new ArrayList();
                components.add(TextComponent.builder().fontSize(4).value("Waiting For:").build());
                components.add(SingleReadOnlyListComponent.builder().list(waitingFor.stream().map(x -> x.getUsername()).toList()).build());

                res.put(user.getUsername(), components);

            } else {

                List components = new ArrayList();
                components.add(TextComponent.builder().fontSize(4).value("You Are:").build());
                components.add(MultiComponents
                        .builder()
                        .numCols(1)
                        .houseCardComponents(Arrays.asList(HouseCardComponent.builder().houseCard(userHouseMap.get(user)).build()))
                        .build());
                components.add(BtnNextComponent.builder().url("game/doneviewhousecard").build());

                res.put(user.getUsername(), components);
            }
        }

        return res;

    }

    public void doneViewHouseCard(Principal principal) {
        userSeenOwnHouseCard.add(userService.findByUsername(principal.getName()));

        if (userSeenOwnHouseCard.size() == userService.findAll().size())
            gameProgress.update(GameProgressData.DRAFT);
    }

    public HouseCard findByUser(MyUser user) {
        return userHouseMap.get(user);
    }

    public void swapHouse(MyUser userA, MyUser userB) {
        HouseCard houseA = userHouseMap.get(userA);
        HouseCard houseB = userHouseMap.get(userB);

        userHouseMap.put(userA, houseB);
        userHouseMap.put(userB, houseA);
    }

    public List<MyUser> findUsersByLoyalty(int loyalty) {

        return userHouseMap.entrySet().stream()
                .filter(x -> x.getValue().getLoyalty() == loyalty).map(x -> x.getKey())
                .collect(Collectors.toList());

    }

    public List<MyUser> findUsersByLoyaltyNot(int notLoyalty) {

        return userHouseMap.entrySet().stream()
                .filter(x -> x.getValue().getLoyalty() != notLoyalty).map(x -> x.getKey())
                .collect(Collectors.toList());

    }
}
