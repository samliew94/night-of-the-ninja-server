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
public class PlayerTokenComponent {

    String username;

    @Builder.Default
    List<Integer> tokens = new ArrayList<>();

}
