# gatling_poc_api_with_auth
Gatling Java POC on API calls with expiring auth


Used Mockoon to mock the /order and /authorize apis.  
***Note: Mockoon environment file is attached and can be used by importing into your local env.***

The /authorize api returns the following:
{"token":"{{now 'YYYYMMDDHHmmssSSS'}}","status":"authorized"}
The token is based on current timestamp, this way you can validate that the token is getting updated properly.

The /order api is POST type route that is just returning http200 on all post requests. There are no checks.
