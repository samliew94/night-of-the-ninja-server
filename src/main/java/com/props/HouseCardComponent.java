package com.props;

import com.house.HouseCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseCardComponent {

    @Builder.Default
    String id = "houseCard";
    HouseCard houseCard;
    String owner;

}
