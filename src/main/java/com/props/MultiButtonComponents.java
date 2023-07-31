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
class MultiButtonComponents {

    @Builder.Default
    String id = "multiButtonComponents";
    List<ButtonComponent> buttonComponents;

    @Builder.Default
    int numCols = 1;

}
