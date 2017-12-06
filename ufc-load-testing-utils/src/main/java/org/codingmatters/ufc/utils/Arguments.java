package org.codingmatters.ufc.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Arguments {

    private enum ParsingState {ARGUMENT, OPTION}

    public static Arguments parse(String ... args) {
        LinkedList<String> arguments = new LinkedList<>();
        HashMap<String, String> options = new HashMap<>();

        if(args != null) {
            ParsingState state = ParsingState.ARGUMENT;
            String currentOption = null;

            for (String arg : args) {
                if(arg.startsWith("-")) {
                    if(ParsingState.OPTION.equals(state)) {
                        options.put(currentOption, null);
                    }
                    state = ParsingState.OPTION;
                    while(arg.startsWith("-")) {
                        arg = arg.substring(1);
                    }
                    currentOption = arg;
                } else {
                    if(state.equals(ParsingState.OPTION)) {
                        options.put(currentOption, arg);
                    } else {
                        arguments.add(arg);
                    }
                    state = ParsingState.ARGUMENT;
                    currentOption = null;
                }
            }

            if(ParsingState.OPTION.equals(state) && currentOption != null) {
                options.put(currentOption, null);
            }
        }
        return new Arguments(
                arguments.toArray(new String[arguments.size()]),
                options
        );
    }

    private final String [] arguments;
    private final HashMap<String, String> options;

    private Arguments(String[] arguments, HashMap<String, String> options) {
        this.arguments = arguments;
        this.options = options;
    }

    public List<String> arguments() {
        return Arrays.asList(this.arguments);
    }


    public boolean hasOptions() {
        return ! this.options.isEmpty();
    }

    public boolean hasOption(String option) {
        return this.options.containsKey(option);
    }

    public String option(String option) {
        return this.options.get(option);
    }

    @Override
    public String toString() {
        return "Arguments{" +
                "arguments=" + Arrays.toString(arguments) +
                ", options=" + options +
                '}';
    }
}
