package com.reveal;

import com.MyUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutcomeResult {

    @Builder.Default
    private Boolean decided = false;

    @Builder.Default
    private Set<MyUser> winners = new LinkedHashSet<>();

    private MyUser mastermindUser;

    @Builder.Default
    private Boolean roninSurvived = false;

    private MyUser ronin;

    private WinOutcome winOutcome;

}
