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
public class MultiRevealComponents {

    @Builder.Default
    String id = "multiRevealComponents";


    @Builder.Default
    List<RevealComponent> revealComponents = new ArrayList<>();

}

