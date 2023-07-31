package com.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleReadOnlyListComponent {

    @Builder.Default
    String id = "singleReadOnlyList";

    List<String> list;

    @Builder.Default
    List<PlayerTokenComponent> playerTokens = new ArrayList<>(); // nullable

}

