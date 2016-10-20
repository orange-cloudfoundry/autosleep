- test isreadytotaketraffic method
- use builder in ProxyMapEntryRepositoryTest
- test different cases with multiple apps (starting/not starting)
- refactor wildcardproxytest & impl with less mocks and duplication
- handle db schema change


Once properly covered:
- start the apps in parallel rather than in sequence
- do not try to delete the routing table for every incoming call (puts burden on the db)