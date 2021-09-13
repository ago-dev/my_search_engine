package validations.impl;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import exceptions.BadRequestException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import validations.InputProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * InputProcessorImpl class is an implementation class for InputProcessor responsible for processing user input
 */
@Log @Getter @Setter
public class InputProcessorImpl implements InputProcessor {

    /**
     * A map that stores a token as key and the corresponding set of ids as its value
     */
    private Map<String, Set<Integer>> tokenIndexes = new HashMap<>();

    /**
     * Process the command given by user
     * @param input the input string given by user
     */
    @Override
    public void processCommand(String input) {
        if (input.startsWith("index ")) {
            processIndexingCommand(input.split(" "));
        } else if (input.startsWith("query ")) {
            processQueryCommand(input.replace("query ", ""));
        }else {
            log.info("Invalid command!");
        }
    }

    /**
     * Process the query command given by user
     * @param expression boolean expression, slice of input given by user
     */
    private void processQueryCommand(String expression) {
        try {
            Expression<String> booleanExpression = ExprParser.parse(expression);
            Expression<String> dnfFormOfBooleanExpression = RuleSet.toDNF(booleanExpression);
            List<Expression<String>> groupsDividedByDisjunctionOperator = dnfFormOfBooleanExpression.getChildren();
            /* groups are divided by | operator unless there is no or operator, then are divided by & operator */
            Set<Integer> allIds;

            if (groupsDividedByDisjunctionOperator.isEmpty() && dnfFormOfBooleanExpression.toLexicographicString().length() != 0) {
                allIds = new HashSet<>(tokenIndexes.get(dnfFormOfBooleanExpression.toLexicographicString()));
            } else {
                allIds = findAllIds(groupsDividedByDisjunctionOperator, expression.contains("|"));
            }

            log.info("query results " + allIds);
        } catch (RuntimeException e) {
            log.info("query error " + e.getMessage());
        }
    }

    /**
     *
     * @param groupsDividedByDisjunctionOperator conjunction expressions created by splitting the main expression using
     *                                           the disjunction operator
     * @return                                   a set of integers that represents the resulting ids
     */
    private Set<Integer> findAllIds(List<Expression<String>> groupsDividedByDisjunctionOperator, boolean hasDisjunctionOperator) {
        Set<Integer> allIds = new HashSet<>();
        Set<Integer> groupIds;

        if (!hasDisjunctionOperator) {
            allIds.addAll(tokenIndexes.get(groupsDividedByDisjunctionOperator.get(0)));
            for (Expression<String> child : groupsDividedByDisjunctionOperator) {
                allIds.retainAll(tokenIndexes.get(child.toLexicographicString()));
            }

            return allIds;
        }

        for (Expression<String> child : groupsDividedByDisjunctionOperator) {
            List<String> tokenGroup = List.of(child.toLexicographicString().replaceAll("[()]", "").split(" & "));

            groupIds = new HashSet<>(tokenIndexes.get(tokenGroup.get(0)));

            if (groupIds.isEmpty())
                continue;

            for (int i = 1; i < tokenGroup.size() && !groupIds.isEmpty(); i++) {
                groupIds.retainAll(tokenIndexes.get(tokenGroup.get(i)));
            }

            allIds.addAll(groupIds);
        }

        return allIds;
    }

    private void processIndexingCommand(String[] args) {
        try {
            int id = validateAndReturnId(args[1]);
            List<String> tokens = validateAndReturnTokens(args);
            indexTokens(tokens, id);
            JSONObject jsonObject = createJsonObjectForDocument(id, tokens);
            createAndWriteDocument(jsonObject);
            log.info("index ok " + id);
            log.info(tokenIndexes.toString());
        } catch (IOException | BadRequestException e) {
            log.info("index error" + e.getMessage());
        }
    }

    private void indexTokens(List<String> tokens, int id) {
        for (String token : tokens) {
            if (tokenIndexes.containsKey(token))
                tokenIndexes.get(token).add(id);
            else
                tokenIndexes.put(token, new HashSet<>(Collections.singletonList(id)));
        }
    }

    private void createAndWriteDocument(JSONObject jsonObject) throws IOException {
        try(FileWriter fileWriter = new FileWriter("./documents/" + UUID.randomUUID() + ".json")) {
            fileWriter.write(jsonObject.toJSONString());
        }
    }

    private JSONObject createJsonObjectForDocument(int id, List<String> tokens) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tokens", tokens);
        jsonObject.put("id", id);
        return jsonObject;
    }

    private List<String> validateAndReturnTokens(String[] args) {
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

    private Integer validateAndReturnId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("index error " + id + "is not an integer");
        }
    }
}
