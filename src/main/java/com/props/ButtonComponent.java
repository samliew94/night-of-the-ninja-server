package com.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class ButtonComponent {

    @Builder.Default
    String id = "buttonComponent";

    @Builder.Default
    String url = "game/btnclick";

    @Builder.Default
    ButtonComponentStyle style = new ButtonComponentStyle();

    @Builder.Default
    Map body = new HashMap();
}
