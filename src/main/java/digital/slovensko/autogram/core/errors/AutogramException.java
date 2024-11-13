package digital.slovensko.autogram.core.errors;

public class AutogramException extends RuntimeException {
    private final String heading;
    private final String subheading;
    private final String description;

    private static final String SIGNING_CERTIFICATE_EXPIRED_EXCEPTION_MESSAGE_REGEX = ".*The signing certificate.*is expired.*";

    public AutogramException(String heading, String subheading, String description, Throwable e) {
        super(e);
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public AutogramException(String heading, String subheading, String description) {
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public String getDescription() {
        return description;
    }

    public static AutogramException createFromIllegalArgumentException(IllegalArgumentException e) {
        for (Throwable cause = e; cause != null && cause.getCause() != cause; cause = cause.getCause()) {
            if (cause.getMessage() == null)
                continue;

            if (cause.getMessage().matches(SIGNING_CERTIFICATE_EXPIRED_EXCEPTION_MESSAGE_REGEX))
                return new SigningWithExpiredCertificateException();
        }

        return new UnrecognizedException(e);
    }

    public boolean batchCanContinue() {
        return true;
    }
}
