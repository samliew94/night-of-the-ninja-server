package com.props;

import com.ninja.NinjaCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class NinjaCardComponent {

    @Builder.Default
    String id = "ninjaCardComponent";

    String url;

    NinjaCard ninjaCard;

    @Builder.Default
    NinjaCardComponentStyle style = new NinjaCardComponentStyle();

    String owner;

}
