package ca.pakdel.namak;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

class Command {
    private static final String[] VALID_CLIENT_TYPES = {
            "local"          // DEFAULT
       // , "local_async"
    };
    private static final String[] VALID_TARGET_TYPES = {
            "glob",          // DEFAULT Bash glob completion - Default
            "pcre",          // Perl style regular expression
            "list",          // Python list of hosts
            "grain",         // Match based on a grain comparison
            "grain_pcre",    // Grain comparison with a regex
            "pillar",        // Pillar data comparison
            "pillar_pcre",   // Pillar data comparison with a regex
            "nodegroup",     // Match on nodegroup
            "range",         // Use a Range server for matching
            "compound",      // Pass a compound match string
            "ipcidr"         // Match based on Subnet (CIDR notation) or IPv4 address.
    };

    private JSONObject commandJSON;
    private String title;
    private String client;
    private String target;
    private String target_type;
    private String function;
    // private List<String> positional_arguments;
    private JSONArray positional_arguments;
    private JSONObject named_arguments;

    private String summary;

/*
    /**
     * Using signature of salt.client.LocalClient().cmd(tgt, fun, arg=(), timeout=None, tgt_type='glob', ret='', jid='', full_return=False, kwarg=None, **kwargs)
     *    and                                 cmd_async(tgt, fun, arg=(),               tgt_type='glob', ret='', jid='',                    kwarg=None, **kwargs)
     * https://docs.saltstack.com/en/latest/ref/clients/index.html#salt.client.LocalClient.cmd
     * timeout, ret, jid, full_return and kwargs are not used
     *
     * @param title Human readable name of the item for use in the UI
     * @param tgt a.k.a target
     * @param fun a.k.a function
     * @param tgt_type  a.k.a targeting type
     * @param arg a.k.a positional positional_arguments
     * @param kwarg a.k.a keyword arguments or named arguments
     */
/*
    private Command(String title, String client, String tgt, String fun, String tgt_type, List<String> arg, JSONObject kwarg) {
        this.title = title;
        target = tgt;
        target_type = tgt_type;
        function = fun;
        positional_arguments = arg;
        named_arguments = kwarg;
        setSummary();
    }
*/

    Command(JSONObject commandJSON) {
        title = commandJSON.optString("title");
        commandJSON.remove("title");

        this.commandJSON = commandJSON;

        client =commandJSON.optString("client", VALID_CLIENT_TYPES[0]);
        target = commandJSON.optString("tgt");
        target_type = commandJSON.optString("tgt_type", VALID_TARGET_TYPES[0]);
        function = commandJSON.optString("fun");
/*        positional_arguments = new ArrayList<>();
        if (commandJSON.has("arg")) {
            JSONArray argArray = commandJSON.optJSONArray("arg");
            for (int i = 0; i < argArray.length(); i++) {
                positional_arguments.add(argArray.optString(i));
            }
        }
*/
        positional_arguments = commandJSON.optJSONArray("kwarg");
        if (positional_arguments == null) {
            positional_arguments = new JSONArray();
        }
        named_arguments = commandJSON.optJSONObject("kwarg");
        if (named_arguments == null) {
            named_arguments = new JSONObject();
        }
        setSummary();
    }

    private void setSummary() {
        summary = function
                + "(" + positional_arguments.toString() + ") "
                + "(" + named_arguments.toString() + ") "
                + " on " + target;
        if (title.isEmpty()) {
            title = function + " on " + target;
        }
/*        if (!isValid()) {
            title = "Invalid command: " + title;
        }
*/
    }

    boolean isValid() {
        return     ! target.isEmpty()
                && ! function.isEmpty()
                && Arrays.asList(VALID_TARGET_TYPES).contains(target_type)
                && Arrays.asList(VALID_CLIENT_TYPES).contains(client);
    }

    String getTitle() {
        return title;
    }

    String getTarget() {
        return target;
    }

    String getFunction() {
        return function;
    }

    JSONArray getPositional_arguments() {
        return positional_arguments;
    }

    String getSummary() {
        return summary;
    }

    JSONObject getPayload() {
        return commandJSON;
    }
}