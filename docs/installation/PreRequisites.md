# Pre-Requisites

## Mongo DB

Install [mongodb](https://www.mongodb.org/) in a location accessible to your Jenkins instance.


## Github Application

Register an [OAuth
Application](https://github.com/settings/applications/new) with GitHub
to obtain client_id and client_secret. **NOTE: The "Authorization
Callback URL" needs to be `<YOUR-JENKINS-URL>/dotci/finishLogin`**.
