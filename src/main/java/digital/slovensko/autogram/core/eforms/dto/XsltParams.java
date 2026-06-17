package digital.slovensko.autogram.core.eforms.dto;

public record XsltParams(String identifier, String language, String destinationType, String target, String mediaType, boolean trustedSource) {

	public XsltParams(String identifier, String language, String destinationType, String target, String mediaType) {
		this(identifier, language, destinationType, target, mediaType, false);
	}
}
