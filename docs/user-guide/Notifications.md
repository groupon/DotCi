## Built-In Notifications

These are common to the built-in Build Types; while its
suggested that any new Build Types utilize them, it is not enforced
through the DotCi Notification Framework. Any Build Type that respects the
`notifications` key can use these:

* [campfire](#-campfire)
* [email](#-email)
* [hipchat](#-hipchat)
* [pusher_email](#-pusher-email)


### `campfire`
```yaml
---
notifications:
  - campfire:
    - room1
    - room2
```
**FIXME: Describe or move out since not in base install**


### `email`
```yaml
---
notifications:
  - email:
    - email1@example.com
    - email2@example.com
```
Sends a notification on failure or recovery (success after failure) to
the single email or array of emails listed.


### `hipchat`
```yaml
---
notifications:
  - hipchat:
    - room1
    - room2
```
**FIXME: Describe or move out since not in base install**


### `pusher_email`
```yaml
---
notifications:
  - pusher_email
```
Sends a notification on failure or recovery (success after failure) to
the person who caused the build to occur.
