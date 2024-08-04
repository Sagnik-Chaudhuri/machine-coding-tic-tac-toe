package org.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Player {
    String id;
    String name;
    PlayerType playerType;
    CellValue markCellValueAs;
}
