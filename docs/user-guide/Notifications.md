## Built-In Notifications

All notifications default to notifying only when there is a failure or recovery( for that particualar branch).

Core Notifications that are bundled with DotCi.

### `email`
```yaml
---
notifications:
  - email:
    - email1@example.com
    - email2@example.com
```

##  Starter-Pack Notifications

There are optional set of notifications that are available if you install [DotCi-Plugins-Starter-Pack](https://github.com/groupon/DotCi-Plugins-Starter-Pack) from update center.

### `campfire`
```yaml
---
notifications:
  - campfire:
    - room1
    - room2
```

### `hipchat` 
( Token for hipchat notifications needs to be configured under global jenkins settings, look for `DotCi Hipchat Configuration`)
```yaml
---
notifications:
  - hipchat:
    - room1
    - room2
#or pass in extra options
  - hipchat: 
      room: test
      notify_on: FAILURE_AND_RECOVERY | ALL
      message: optional message
```


