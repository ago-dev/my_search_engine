package validations;

import exceptions.BadRequestException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for validating user commands
 */
public final class CommandValidator {
    private CommandValidator() {}

    public static List<String> validateAndReturnTokens (String[] args){
        if (args.length < 3)
            throw new BadRequestException("index error You should input at least one token");

        List<String> tokens = new ArrayList<>();

        for (int i = 2; i < args.length; i++) {
            if (!StringUtils.isAlphanumeric(args[i]))
                throw new BadRequestException("index error " + "Token: " + args[i] + " is not alphanumeric");
            tokens.add(args[i]);
        }

        return tokens;
    }

    public static Integer validateAndReturnId (String id){
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("index error " + id + "is not an integer");
        }
    }
}
