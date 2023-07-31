package com.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleSelectableListComponent {

    @Builder.Default
    String id = "singleSelectableList";
    List list;
    int numToSelect;
    String url;
    List selected;

    @Builder.Default
    Map body = new LinkedHashMap();

}
