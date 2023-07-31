package com.honor;

import com.MyUser;
import com.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class HonorService {

    @Autowired
    UserService userService;

    Map<MyUser, List<HonorToken>> userHonorMap = new HashMap<>();

    Random random = new SecureRandom();

    public void reset() {
        userHonorMap.clear(); // clear all user's earned HONOR tokens
    }

    public int getNumHonor(MyUser user) {
        userHonorMap.putIfAbsent(user, new ArrayList<>());
        return userHonorMap.get(user).size();
    }

    public List<HonorToken> findAllByUser(MyUser user) {
        userHonorMap.putIfAbsent(user, new ArrayList<>());
        return userHonorMap.get(user);
    }

//    public List<HonorTokenComponent> findAllHonorTokenComponentsByUser(MyUser user, String bodyId, int... visibleIndexes) {
//
//        List<Integer> showIndexes = new ArrayList<>();
//        for (int visibleIndex : visibleIndexes)
//            showIndexes.add(visibleIndex);
//
//        var all = findAllByUser(user);
//
//        List<HonorTokenComponent> honorTokenComponents = IntStream.range(0, all.size())
//                .mapToObj(i -> HonorTokenComponent
//                        .builder()
//                        .honorToken(
//                                visibleIndexes.length == 0
//                                        ? all.get(i)
//                                        : showIndexes.contains(i)
//                                        ? all.get(i)
//                                        : HonorToken.builder().build()) // user are not supposed to see them.
//                        .body(new HashMap() {{
//                            put("id", "spirit-lookathonortoken");
//                            put("index", i);
//                        }})
//                        .build())
//                .collect(Collectors.toList());
//
//        return honorTokenComponents;
//
//    }

    public void swap(MyUser userA, int indexA, MyUser userB, int indexB) {

        List<HonorToken> honorTokensA = userHonorMap.get(userA);
        List<HonorToken> honorTokensB = userHonorMap.get(userB);

        HonorToken htA = honorTokensA.get(indexA);
        HonorToken htB = honorTokensB.get(indexB);

        honorTokensA.set(indexA, htB);
        honorTokensB.set(indexB, htA);

    }

    public void giveRandomHonorToken(MyUser user) {

        userHonorMap.putIfAbsent(user, new ArrayList<>());

        userHonorMap.get(user).add(HonorToken.builder()
                .value(random.nextInt(2, 5))
                .build());

    }

    public int findSumOfTokens(MyUser user) {
        return findAllByUser(user).stream().mapToInt(x -> x.getValue()).sum();
    }

    public List<MyUser> findAllWithTotalTokensGreaterThan(MyUser user) {

        List<MyUser> users = new ArrayList<>();
        int curUserSize = findAllByUser(user).size();
        userHonorMap
                .entrySet()
                .forEach(e -> {
                    if (e.getKey().getUsername().equalsIgnoreCase(user.getUsername()))
                        return;

                    if (e.getValue().size() > curUserSize) {
                        users.add(e.getKey());
                    }
                });

        return users;
    }

    public void takeRandomHonorTokenFromAndGiveTo(MyUser from, MyUser to) {

        List<HonorToken> fromHonorToken = userHonorMap.get(from);
        int took = random.nextInt(0, fromHonorToken.size());
        HonorToken removed = userHonorMap.get(from).remove(took);

        userHonorMap.get(to).add(removed);
        Collections.shuffle(userHonorMap.get(to));

    }

    public Map<MyUser, List<HonorToken>> findAll() {
        return userHonorMap;
    }

    /**
     * use this in debug mode to test things out
     */
    public void debugMode() {

        List<MyUser> all = userService.findAll();

        for (int i = 0; i < all.size(); i++) {
            if (i > 0)
                giveRandomHonorToken(all.get(i));
        }

    }
}

