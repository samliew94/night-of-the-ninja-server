package com.honor;

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
public class UltimateWinner {

    @Builder.Default
    private Set<MyUser> winners = new LinkedHashSet<>();

    private Integer highscore;

}
