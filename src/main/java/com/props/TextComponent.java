package com.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextComponent {

    @Builder.Default
    String id = "text";
    int fontSize;
    String value;

    TextComponentStyle style;
}

