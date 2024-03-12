# CatBase server

## Configuration file

It exists, and allows full configuration of the server

```yaml
port: 52137 # Server port
users:
  - login: "login"
    password: "zaq1@WSX"
queues: # All message queues
  - name: "Queue 1"
    size: 100 # Default is 200
  - name: "Queue 2"
exchanges: # All message exchanges
  - name: "Exchange 1"
    type: "direct" # A message exchange type, default is "direct"
                   # Available types: ["direct"]
    queues: # Queue references
      - "Queue 1"
      - "Queue 2"
```