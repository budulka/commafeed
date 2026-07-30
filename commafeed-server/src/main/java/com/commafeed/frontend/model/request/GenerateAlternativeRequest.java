package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Generate alternative request")
@Data
public class GenerateAlternativeRequest implements Serializable {

    @NotEmpty
    @Schema(description = "target field: 'title' or 'content'", required = true)
    private String target;

    @NotEmpty
    @Schema(description = "instruction prompt for LLM", required = true)
    private String prompt;
}
