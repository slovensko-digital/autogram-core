package digital.slovensko.autogram.core.server.dto;

import digital.slovensko.autogram.core.BaselineLevel;

public record ExtensionRequestBody (BaselineLevel targetLevel, Document document) {
}
