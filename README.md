# Namak
Salt Net API client

Sample Dashboard: https://www.dropbox.com/s/vs4q2jfpnkj5d2w/dashboard-sample.json?dl=1
## Dashboard
Dashboards are JSON arrays of JSON objests with following attributes:
- title (optional): It defaults to "[ Asynchronous | Synchronous ] execution of %fun$s on %tgt$s with [no arguments | following arguments: %arg$s]"
- client: local or local_async
- tgt: shell-style globbing minion target
- fun: function to run
- arg (optional): arguments to pass to the function
The only attribute that is not a direct map of [Salt API](http://docs.saltstack.com/en/latest/ref/clients/) is the title, which is optional.

## FIXME
- Should we just increase SaltMaster pref no., instead of finding holes in the sequence?

## TODO
- Dashboards
    - can be absolute or relative
    - can have their own Timeout
    - Do not reload all of them, if just one is changed
- The trailing slash of Salt Master address should be optional
- Add some documentation
- Add more verbose error messages
- Grab and cache a list of Minions
- Make sure Volley does not cache execution URLs
- Execution Activity:
    - re-run option

- Validate URLs in EditTextPreference
    - use onPreferenceChangeListener

- Ask for Garbage Collection while waiting for network
- Re-Implement relative dashboard URL:
    - Relative Dashboards need to be reloaded by switching to another Salt Master
