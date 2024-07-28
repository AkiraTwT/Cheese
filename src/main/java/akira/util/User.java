package akira.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String discordId;
    private String surname;
    private int absences;
    private int excusedAbsences;
}