package org.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Move {
    Player player;
    int rowValue;
    int columnValue;
}
