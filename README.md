# Namak
Salt Net API client

Sample Dashboards:
- This one has a bunch of faulty items for testing:
 https://www.dropbox.com/s/vs4q2jfpnkj5d2w/dashboard-sample.json?dl=1
- This just has one item: "Minion IPs":
 https://www.dropbox.com/s/hd6hzrdlgb9ltfm/dashboard-sample2.json?dl=1


## Dashboard
You can add up to 99 dashboards, each with up to 99 commands.
Dashboards are JSON arrays of JSON objects with following attributes:
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
    - can have their own Timeout
    - can be absolute or relative
- Re-Implement relative dashboard URL:
    - Relative Dashboards need to be reloaded by switching to another Salt Master
- Add more verbose error messages
- Grab and cache a list of Minions
- Make sure Volley does not cache execution URLs
- Execution Activity:
    - re-run option

- onDestroy: log out
- onTrimMemory: clean up dashboards and stuff ....
- onPause: memorize current SaltMaster and authToken
- Testing:
    - permissions


## Error Code categories
100 NamakApplication
200 MainActivity
300 DashboardAdapter
400 SaltMaster
500 GeneralSettingsActivity / DashboardSettingsActivity / SaltMasterSettingsActivity


            Popup.error(mainActivity, getString(R.string.should_never_happen), 200, null);
//            Popup.error(mainActivity, getString(R.string.incomplete_settings), 200, null);
//            mDrawerLayout.closeDrawers();

Popup.error(this, getString(R.string.incomplete_settings), 200, null);
Popup.error(mainActivity, getString(R.string.should_never_happen), 201, null);
Popup.error(mainActivity, getString(R.string.not_logged_in), 202, null);


FIXME open when added SM or Dash
FIXME do not refresh if not dashboard



Settings: Do n ot go back if not complete

DEVICE SHELL COMMAND: pm uninstall com.amirpakdel.namak
DEVICE SHELL COMMAND: pm install -r "/data/local/tmp/com.amirpakdel.namak"
