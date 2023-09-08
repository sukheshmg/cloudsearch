# CloudSearch
Platform to aggregate multiple cloud document stores and perform fast searches on them

For Architecture Details, check [here] (https://docs.google.com/document/d/1S3Or46YBN5vmh5twAFsmhrHvNwaAG-w3nnZ_AfgPGYM/edit?usp=sharing)

## Main Concepts

### Project
A Project is a collection of multiple cloud accounts, possibly owned by multiple people/entities.
Projects help in organizing multiple accounts and perform search on them as a whole.
Typical usage could be
- A single user having multiple Google Drive accounts
- A single user having Google Drive, DropBox or other OAuth2 Compatible doc storage accounts
- An organization with multiple doc storage accounts

### Channel
A Channel represents a single Doc Storage account.
Channel stores the user's OAuth2 tokens. Orchestrating the OAuth flow including procuring user consent is outside the scope of this platform.

## Usage
Typical flow would be this
- create a Project
- create one more channels
- add channels to project
- start channels so that data can be indexed
- perform search and relax!

## Apis

### Create Project
Create a demo project named demoproject as below
```
curl --location 'https://host:port/v1/project' \
--header 'Content-Type: application/json' \
--data '{"canonicalName":"demoproject"}'
```

### Create Channel
Create a demochannel named demochannel as below
```
curl --location 'https://host:port/v1/channel' \
--header 'Content-Type: application/json' \
--data '{
    "canonicalName" : "demochannel",
    "accessToken": "<oauth2_access_token>",
    "refreshToken": "<oauth2_refresh_token>"
}'
```

### Add channel to project
Add demochannel to demoproject as below
```
curl --location 'https://host:port/v1/channel/demochannel/projects' \
--header 'Content-Type: application/json' \
--data '{
    "projects": ["demoproject"]
}'
```

### Start channel
Start demochannel as below
```
curl --location --request POST 'https://host:port/v1/channel/demochannel'
```

### Search for queryString in project
Search for queryString in demoproject as below
```
curl --location 'https://host:port/v1/search?q=queryString&project=demoproject'
```
