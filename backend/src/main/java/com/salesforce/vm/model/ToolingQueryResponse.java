package com.salesforce.vm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolingQueryResponse<T> {
    @JsonProperty("size")
    private int size;

    @JsonProperty("totalSize")
    private int totalSize;

    @JsonProperty("done")
    private boolean done;

    @JsonProperty("records")
    private List<T> records;
}
