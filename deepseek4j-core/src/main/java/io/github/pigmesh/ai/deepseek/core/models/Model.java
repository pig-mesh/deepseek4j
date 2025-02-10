package io.github.pigmesh.ai.deepseek.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Model {
    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String modelObject;

    @JsonProperty("owned_by")
    private String ownedBy;

}