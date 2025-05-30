package org.platform.spidereddit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdge {
    private String source;
    private String target;
    private int weight;
}
