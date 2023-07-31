package com;

import com.honor.AwardService;
import com.honor.HonorService;
import com.house.HouseService;
import com.lifeanddeath.JudgeBlindShinobiService;
import com.lifeanddeath.LifeAndDeathService;
import com.lobby.LobbyService;
import com.ninja.NinjaCard;
import com.ninja.NinjaCardService;
import com.reveal.RevealService;
import com.spymystic.SpyMysticService;
import com.trickster.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
@RestController
@RequestMapping("game")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GameController {

    @Autowired
    ApplicationContext context;

    @Autowired
    FooService fooService;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    GameProgressService gameProgress;

    @Autowired
    HouseService houseService;
    @Autowired
    NinjaCardService ninjaCardService;

    @Autowired
    DraftService draftService;

    @Autowired
    SpyMysticService spyMysticService;
    @Autowired
    RevealService revealService;

    @Autowired
    private UserService userService;

    @Autowired
    private HonorService honorService;

    @Autowired
    private TricksterService tricksterService;
    @Autowired
    private TricksterGraveService tricksterGraveService;
    @Autowired
    private TricksterShapeService shapeService;
    @Autowired
    private TricksterTroubleService troubleService;
    @Autowired
    private TricksterSpiritService spiritService;
    @Autowired
    private TricksterThiefService thiefService;
    @Autowired
    private JudgeBlindShinobiService judgeBlindShinobiService;

    @Autowired
    private LifeAndDeathService lifeAndDeathService;

    @Autowired
    private AwardService awardService;

    @Autowired
    private SelectorService selectorService;

    /**
     * publishes message to all users ;
     */
    public void update() throws Exception {

        Map data = null;

        GameProgressData gpd = gameProgress.get();

        if (gpd == GameProgressData.LOBBY)
            data = context.getBean(LobbyService.class).getLobbyData();
        if (gpd == GameProgressData.VIEWHOUSE)
            data = houseService.getHouseData();
        if (gpd == GameProgressData.DRAFT)
            data = draftService.getData();
        if (gpd == GameProgressData.SPY_MYSTIC)
            data = spyMysticService.getData();
        if (gpd == GameProgressData.TRICKSTER)
            data = tricksterService.getData();
        if (gpd == GameProgressData.BLIND || gpd == GameProgressData.SHINOBI)
            data = judgeBlindShinobiService.getData();
        if (gpd == GameProgressData.REVEAL)
            data = revealService.getData();
        if (gpd == GameProgressData.AWARD_TOKEN)
            data = awardService.getData();

        if (gpd == GameProgressData.SPY_MYSTIC
                || gpd == GameProgressData.TRICKSTER
                || gpd == GameProgressData.BLIND
                || gpd == GameProgressData.SHINOBI) {
            ninjaCardService.addUsedAndActivatedCardsAtFirst(data);
        }

        if (gpd != GameProgressData.LOBBY)
            userService.addHardResetComponent(data);

        context.getBean(MyWebSocketHandler.class).broadcast(data);

    }

    @PostMapping("start")
    public void start() throws Exception {
        start(true);
    }

    private void start(boolean hardReset) throws Exception {

        selectorService.reset(null);
        houseService.reset();
        ninjaCardService.reset();
        draftService.reset();
        spyMysticService.reset();
        tricksterService.reset();
        lifeAndDeathService.reset();
        revealService.reset();

        if (hardReset)
            honorService.reset();

        awardService.reset();

        gameProgress.update(GameProgressData.VIEWHOUSE);

        update();

    }

    @PostMapping("doneviewhousecard")
    public void doneViewHouseCard(Principal principal) throws Exception {

        houseService.doneViewHouseCard(principal);
        update();
    }

    @PostMapping("draftpicked")
    public synchronized void draftPickedMap(Principal principal, @RequestBody NinjaCard ninjaCard) throws Exception {

        draftService.draftPicked(principal, ninjaCard);
        update();

    }

    @PostMapping("play")
    public synchronized void play(Principal principal, @RequestBody NinjaCard ninjaCard) throws Exception {

        GameProgressData gameProgressData = gameProgress.get();

        if (gameProgressData == GameProgressData.SPY_MYSTIC)
            spyMysticService.play(principal, ninjaCard);
        else if (gameProgressData == GameProgressData.TRICKSTER)
            tricksterService.play(principal, ninjaCard);
        else if (gameProgressData == GameProgressData.BLIND || gameProgressData == GameProgressData.SHINOBI)
            judgeBlindShinobiService.play(principal, ninjaCard);

        update();

    }

    @PostMapping("selectusername")
    public synchronized void selectUsername(Principal principal, @RequestBody Map body) throws Exception {

        String selected = (String) body.get("selected");

        selectorService.addSelected(selected);

        update();
    }

    @PostMapping("selectninjacard")
    public synchronized void selectNinjaCard(Principal principal, @RequestBody NinjaCard ninjaCard) throws Exception {
        GameProgressData gameProgressData = gameProgress.get();

        if (gameProgressData == GameProgressData.TRICKSTER) {
            tricksterService.selectNinjaCard(principal, ninjaCard);
        }

        update();
    }


    @PostMapping("next")
    public synchronized void next(Principal principal) throws Exception {
        GameProgressData gameProgressData = gameProgress.get();

        if (gameProgressData == GameProgressData.TRICKSTER)
            tricksterService.end(principal);

        update();
    }

    @PostMapping("btnclick")
    public synchronized void btnClick(Principal principal, @RequestBody Map body) throws Exception {

        if (body != null) {

            String id = (String) body.get("id");

            if (id != null) {
                if (id.equals("spy-mystic-end"))
                    spyMysticService.end(principal);

                if (id.equals("spy-mystic-confirmselection"))
                    spyMysticService.confirmSelection(principal, body);

                if (id.equals("trickster-end"))
                    tricksterService.end(principal);

                if (id.equals("grave-useorkeep"))
                    tricksterGraveService.onUseOrKeep(principal, body);

                if (id.equals("shape-confirmselection"))
                    shapeService.confirmSelection();

                if (id.equals("shape-swap"))
                    shapeService.onSwap(principal, body);

                if (id.equals("shape-end"))
                    shapeService.end(principal);

                if (id.equals("trouble-confirmselection"))
                    troubleService.confirmSelection();

                if (id.equals("trouble-reveal"))
                    troubleService.reveal(principal, body);

                if (id.equals("trouble-end"))
                    troubleService.end(principal);

                if (id.equals("spirit-confirmselection"))
                    spiritService.confirmSelection();

                if (id.equals("spirit-lookathouseorhonor"))
                    spiritService.lookAtHouseOrHonor(body);

                if (id.equals("spirit-sawhousecard"))
                    spiritService.sawHouseCard(body);

                if (id.equals("spirit-lookathonortoken"))
                    spiritService.lookAtHonorToken(body);

                if (id.equals("spirit-choosehonortokentoswap"))
                    spiritService.chooseHonorTokenToSwap(body);

                if (id.equals("spirit-swap"))
                    spiritService.swap(body);

                if (id.equals("spirit-end"))
                    spiritService.end(principal);

                if (id.equals("thief-confirmselection"))
                    thiefService.confirmSelection();

                if (id.equals("thief-end"))
                    thiefService.end(principal);

                if (id.equals("jbs-shinobiseehousecard"))
                    judgeBlindShinobiService.shinobiSeeHouseCard();

                if (id.equals("kill"))
                    judgeBlindShinobiService.kill(body);

                if (id.equals("jbs-end"))
                    judgeBlindShinobiService.end(principal);

                if (id.equals("reveal-awardtoken"))
                    gameProgress.update(GameProgressData.AWARD_TOKEN);

                if (id.equals("endornextround"))
                    awardService.endOrNextRound();

                if (id.equals("startgame"))
                    start();

                if (id.equals("hardreset")) {
                    gameProgress.update(GameProgressData.LOBBY);
                }

            }

        }

        update();
    }

    public synchronized void nextRound() throws Exception {
        start(false);
    }
}

