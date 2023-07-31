package com.props;

import com.honor.HonorToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HonorTokenComponent {

    @Builder.Default
    String id = "honorTokenComponent";

    @Builder.Default
    String url = "game/btnclick";

    @Builder.Default
    Map body = new LinkedHashMap();

    HonorToken honorToken;

    @Builder.Default
    int highlight = -1;

}
