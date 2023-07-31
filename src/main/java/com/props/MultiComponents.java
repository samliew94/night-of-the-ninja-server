package com.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class MultiComponents {

    @Builder.Default
    String id = "multiComponents";
    private List<NinjaCardComponent> ninjaCardComponents;
    private List<HouseCardComponent> houseCardComponents;
    private List<HonorTokenComponent> honorTokenComponents;

    @Builder.Default
    private int numCols = 2;

    private MultiComponentsStyle style;

    private String title;

}

