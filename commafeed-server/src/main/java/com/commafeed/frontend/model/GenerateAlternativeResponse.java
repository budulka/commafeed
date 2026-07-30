package com.commafeed.frontend.model;

import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Generate alternative response")
@Data
public class GenerateAlternativeResponse implements Serializable {

    @Schema(description = "original entry (id, title, content)")
    private OriginalEntry original;

    @Schema(description = "target field: 'title' or 'content'")
    private String target;

    @Schema(description = "prompt sent to LLM")
    private String prompt;

    @Schema(description = "generated alternative text")
    private String alternative;

    @Data
    public static class OriginalEntry {
        private String id;
        private String title;
        private String content;

        public OriginalEntry() {}

        public OriginalEntry(String id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }
    }
}
