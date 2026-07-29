package com.commafeed.backend.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

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
