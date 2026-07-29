package com.commafeed.backend.rest.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Note Request")
@Data
public class NoteRequest implements Serializable {

    @Schema(description = "entry id", required = true)
    @NotNull
    private Long entryId;

    @Schema(description = "note text", required = true)
    @NotNull
    @Size(max = 500)
    private String text;

    @Schema(description = "rating (0-5)")
    private Integer rating;
}
