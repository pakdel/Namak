# Error Codes

| CODE |                          DETAILS                                     |
| ---- | -------------------------------------------------------------------- |
|  000 | Is added to non-production releases for testing |
|  1xx | __Errors coming from NamakApplication__ |
|  100 | Could not load the dashboard. Please check your dashboard’s URL to be correct and the contents of the page to be a JSON array as documented in the README.md file. |
|  101 | Could not load the dashboard. Got a null response when trying to get the Dashboard URL. |
|  102 | Could not load the dashboard. Please make sure your dashboard has a valid JSON with the syntax specified in the documentation. |
|  103 | This should never happen! In dashboardJSON neither "title" key is null, nor the value is non-finite number. |
|  104 | Dashboard is too big! Each dashboard can have up to 99 commands. |
|  2xx | __Errors coming from MainActivity__ |
|  200 | Incomplete settings. You will need at least one Salt Master and one Dashboard set up. Open settings page from the menu (or the wrench icon in the title bar). |
|  201 | Not logged in. Open the left drawer and choose a SaltMaster to log into. |
|  202 | Session Expired. Either relogin using the button on the bottom or open the left drawer and choose a SaltMaster to log into. |
|  3xx | __Errors coming from DashboardAdapter__ |
|  301 | This should never happen! In getChild, we always get an item that exists. |
|  302 | This should never happen! In getChildView, we already know that "title" exists. |
|  4xx | __Errors coming from SaltMaster__ |
|  401 | This should never happen! In SaltMaster neither of "eauth", "username" and "password" keys is null, nor the value is non-finite number. |
|  402 | This should never happen! We are generating relative URLs ourselves. |
|  405 | Unexpected response during login! Verify the Salt Net API service to be up and responding properly to login requests. |
|  410 | Login failed! Verify your SaltMaster address and credentials. |
|  411 | Login timed out! Verify the SaltMaster address to be correct and the Salt API server to be up and running. |
|  412 | Connection to SaltMaster failed! Verify the SaltMaster address and your network access. |
|  5xx | __Errors coming from NamakSettingsActivity (GeneralSettingsActivity / DashboardSettingsActivity / SaltMasterSettingsActivity)__ |
|  500 | Incomplete settings. At least one Salt Master and one Dashboard are required, with all of their fields set (even if the password is empty, it needs to be set).  |
|  6xx | __Errors coming from CommandExecutionActivity__ |
|  600 |  This should never happen! MainView always sends an item position that exists. |
|  601 |  No client type found! Client field of dashboard item is required. |
|  602 |  Client type not supported! Only supported client tyes are local and local_async. |
|  603 |  fun not found! Function (fun) field of dashboard item is required. |
|  604 |  tgt not found! Target (tgt) field of dashboard item is required. |
|  610 |  This should never happen! onActivityResult should never receive any requestCode other than COMMAND_ADJUST_REQUEST. |
|  611 |  This should never happen! CommandModificationActivity always returns a valid JSON. |
|  612 |  This should never happen! savedInstanceState.getString("returned_data") always returns a valid JSON. |
|  620 | Failed to run the command! Please verify the command and the Salt Net API service to be up and running. |
|  621 | Unexpected response during command execution! Either Synchronous execution did not return a "return" array, or Asynchronous execution did not return a "jid". |
|  622 | Failed to check for execution results! Please verify the JID and the Salt Net API service to be up and running. |
|  623 | Unexpected response when checking for execution results! Did not receive a "return" array from server. |
|  7xx | __Errors coming from CommandModificationActivity__ |
|  700 |  This should never happen! CommandExecutionActivity always sends a valid JSON. |
|  701 |  This should never happen! CommandExecutionActivity always sends a valid client type. |


```java
Popup.error(NamakApplication.foregroundActivity, context.getString(R.string.should_never_happen), 103, error);
// Shows a "This should never happen!" error message.
```
