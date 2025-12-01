package digital.slovensko.autogram.core.errors;

public class PasswordNotProvidedException extends AutogramException {
    public PasswordNotProvidedException() {
        super("Nastala chyba", "Nezadali ste podpisový PIN alebo heslo", "Pravdepodobne ste len zavreli okno na zadanie podpisového PINu alebo hesla. Skúste znova.");
    }
}
