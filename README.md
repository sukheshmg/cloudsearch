# CloudSearch
Platform to aggregate multiple cloud document stores and perform fast searches on them

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
- perform search and relax!

## Apis

### Create Project
Create a demo project names demoproject as below
`
curl --location 'https://host:port/v1/project' \
--header 'Content-Type: application/json' \
--data '{"canonicalName":"demoproject"}'
`
