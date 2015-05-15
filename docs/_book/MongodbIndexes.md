#### Builds for project
```db.run.ensureIndex( {projectId: 1 } )```

#### Builds by number
```db.run.ensureIndex( {number: 1 } )```

#### Builds by Result and Project
```db.run.ensureIndex( {projectId: 1, result: 1 } )```
#### Builds for user
```db.run.ensureIndex( {className: 1, 'actions.causes.user': 1, 'actions.causes.pusher': 1 } )```

#### Builds capped for 30 days
```db.run.ensureIndex( { "timestamp": 1 }, { expireAfterSeconds: 2592000 } )```

