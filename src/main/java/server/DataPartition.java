// DataPartition.java
package server;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataPartition {
    private String partitionId;
    private List<String> data;
}
