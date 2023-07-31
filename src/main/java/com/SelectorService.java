package com;

import com.trickster.TricksterService;
import com.trickster.TricksterStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SelectorService {

    @Autowired
    private UserService userService;

    @Autowired
    private GameProgressService gameProgress;

    @Autowired
    @Lazy
    private TricksterService tricksterService;

    private String selecting;
    private List<String> selected = new ArrayList<>();

    public void reset(String selectingUsername) {
        selecting = selectingUsername;
        selected.clear();
    }


    public void addSelected(String s) {

        GameProgressData gameProgressData = gameProgress.get();

        int max = 1;

        if (gameProgressData == GameProgressData.TRICKSTER) {
            if (tricksterService.getTricksterStage() == TricksterStage.ACTIVATE_SHAPE) {
                max = 2;
            }
        }

        if (selected.contains(s))
            return;

        selected.add(s);

        while (selected.size() > max)
            selected.remove(0);


    }

    public String getSelecting() {
        return selecting;
    }

    public String lastSelected() {
        return selected.get(selected.size() - 1);
    }

    public List<String> getSelected() {
        return selected;
    }
}
