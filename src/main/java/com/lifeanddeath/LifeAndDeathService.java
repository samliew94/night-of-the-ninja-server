package com.lifeanddeath;

import com.MyUser;
import com.UserService;
import com.honor.HonorService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

enum KillResult {
    SUCCESS,
    MIRROR_MONKED,
    MARTYRED
}

@Service
public class LifeAndDeathService {

    @Autowired
    UserService userService;

    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    HonorService honorService;

    List<LastKillResult> lastKillResults = new ArrayList<>();

    public void reset() {
        lastKillResults.clear();
    }

    public List<MyUser> findAllByIsDead(boolean dead) {

        if (dead) {
            return lastKillResults.stream().map(x -> x.getActuallyDead()).toList();
        } else {
            List<MyUser> all = userService.findAll();
            all.removeAll(lastKillResults.stream().map(x -> x.getActuallyDead()).toList());
            return all;
        }

    }

    public List<MyUser> findAllByIsDead(boolean dead, MyUser except) {

        List<MyUser> all = findAllByIsDead(dead);
        all.remove(except);

        return all;
    }

    public void kill(KillIntention killIntention, NinjaCard source) {

        if (killIntention.getIntendedTarget() == null) { // nobody was even a target

            lastKillResults.add(LastKillResult.builder()
                    .killer(killIntention.getKiller())
                    .intendedTarget(null)
                    .actuallyDead(null)
                    .martyred(false)
                    .mirrorMonked(false)
                    .source(source)
                    .build());

            return;

        }

        if (source.getCode().equals("shinobi") && !killIntention.getShinobiDecidedToKill()) {

            lastKillResults.add(LastKillResult.builder()
                    .killer(killIntention.getKiller())
                    .intendedTarget(killIntention.getIntendedTarget())
                    .actuallyDead(null)
                    .martyred(false)
                    .mirrorMonked(false)
                    .source(source)
                    .build());

            return;

        }

        // someone actually gonna die
        List<NinjaCard> cardsOnHand = ninjaCardService.onHandMap().get(killIntention.getIntendedTarget());

        NinjaCard monk = cardsOnHand.stream().filter(x -> x.getCode().equals("monk")).findFirst().orElse(null);
        NinjaCard martyr = cardsOnHand.stream().filter(x -> x.getCode().equals("martyr")).findFirst().orElse(null);

        // if victim holding martyr and killed by blind/shinobi, give honor token
        if (martyr != null && (source.getCode().equals("blind") || source.getCode().equals("shinobi"))) {
            ninjaCardService.removeFromHand(killIntention.getIntendedTarget(), martyr);
            honorService.giveRandomHonorToken(killIntention.getIntendedTarget());
        }

        MyUser actuallyDead = killIntention.getIntendedTarget();

        // if victim holding monk and killed by blind/shinobi, reverse the kill
        if (monk != null && (source.getCode().equals("blind") || source.getCode().equals("shinobi"))) {
            ninjaCardService.removeFromHand(killIntention.getIntendedTarget(), monk);
            actuallyDead = killIntention.getKiller();
        }

        lastKillResults.add(LastKillResult.builder()
                .killer(killIntention.getKiller())
                .intendedTarget(killIntention.getIntendedTarget())
                .actuallyDead(actuallyDead)
                .martyred(martyr != null && !source.getCode().equals("judge"))
                .mirrorMonked(monk != null && !source.getCode().equals("judge"))
                .source(source)
                .build());

        // prevent victim from playing anymore cards.
        ninjaCardService.removeAllToBeActivated(actuallyDead);
        ninjaCardService.removeAllOnHand(actuallyDead);

    }

    public boolean isDead(MyUser user, boolean dead) {

        if (dead)
            return lastKillResults.stream().anyMatch(x -> x.getActuallyDead() != null && x.getActuallyDead().getUsername().equalsIgnoreCase(user.getUsername()));
        else
            return lastKillResults.stream().noneMatch(x -> x.getActuallyDead() != null && x.getActuallyDead().getUsername().equalsIgnoreCase(user.getUsername()));

    }

    public LastKillResult getLatestKill() {
        return lastKillResults.get(lastKillResults.size() - 1);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LastKillResult {

    private MyUser killer;
    private MyUser intendedTarget;
    private MyUser actuallyDead;

    @Builder.Default
    private Boolean mirrorMonked = false;

    @Builder.Default
    private Boolean martyred = false; // did the victim get honor points?
    private NinjaCard source; // what card initiated this?


}