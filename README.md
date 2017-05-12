# Namak
Salt Net API client

Here is the Alpha testing opt-in link: https://play.google.com/apps/testing/com.amirpakdel.namak

Sample Dashboards:
- This one has a bunch of faulty items for testing:
 https://www.dropbox.com/s/vs4q2jfpnkj5d2w/dashboard-sample.json?dl=1
- This just has one item: "Minion IPs":
 https://www.dropbox.com/s/hd6hzrdlgb9ltfm/dashboard-sample2.json?dl=1


## Dashboard
You can add up to 99 dashboards, each with up to 99 commands.
Dashboards are JSON arrays of JSON objects with following attributes:
- title (optional): It defaults to "[ Asynchronous | Synchronous ] execution of %fun$s on %tgt$s with [no arguments | following arguments: %arg$s]"
- client: local, local_async or runner (runner_async is not supported)
- tgt: minion target for local and local_async client types
- expr_form (optional): Minion targeting expression form for local and local_async client types. defaults to "glob"
- fun: function to run
- arg / args (optional): arguments to pass to the function. It is called arg for local and local_async clients and args for runner client (Not sure why!)
The only attribute that is not a direct map of [Salt API](http://docs.saltstack.com/en/latest/ref/clients/) is the title, which is optional.

## TODO
- Keep last JID of every dashboard item
- Sort Salt Masters by name
- Support modification of kwargs
- Filter command outputs
- In CommandModificationActivity: Make a field readonly if cannot edit!
- Dashboards
    - can have their own Timeout
    - can be absolute or relative
        - Re-Implement relative dashboard URL: need to be reloaded by switching to another Salt Master
- Add more verbose error messages
- Make sure Volley does not cache execution URLs
- Execution Activity:
    - re-run option
- onDestroy: log out
- onTrimMemory: clean up dashboards and stuff ....
- onPause: memorize current SaltMaster and authToken
- Testing:
    - permissions
- Grab and cache a list of Minions
- Should we just increase SaltMaster pref no., instead of finding holes in the sequence?


## Error Code categories
- 100 NamakApplication
- 200 MainActivity
- 300 DashboardAdapter
- 400 SaltMaster
- 500 GeneralSettingsActivity / DashboardSettingsActivity / SaltMasterSettingsActivity
- 600 CommandExecutionActivity
- 700 CommandModificationActivity


### Validations before pull request
- TODO
- FIXME
- "Here" comments
- Toast
- Duplicate Popup.error
- Popup.error without getString
- setText
- Verify Log.e and Log.d
