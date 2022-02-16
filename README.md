## **A free opensource domain tracker with a breakdown of which countries players connected with for each domain versions 1.13+**

> **Command usage: /domaintracker <MM/dd/yyyy>** **permission: domaintracker.admin**

# Examples
![Image](https://i.imgur.com/wzs4MxS.png)
![Image](https://i.imgur.com/YAzJu6c.png)

## Default config / setup guide
```
mysql:
  address: "localhost"
  port: 3306
  database: "minecraft"
  username: "root"
  password: ""
  useSSL: true

# Register on here https://www.maxmind.com/en/geolite2/signup?lang=en for a free GeoLite2 license key
# Then fill in your license key below and restart the plugin
database:
  license-key: ""
  download-url: "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key={LICENSEKEY}&suffix=tar.gz"
  download-if-missing: true
  update:
    enabled: true
    every-x-days: 30
    
# Turning this setting to true will make it only count the join if they have never joined the server before
only-count-unique: false

# Fill in your domains you wish to track joins for below
domains:
  - "play.example.net:25565"
  - "hub.example.net:25565"
  - "fun.example.net:25565"

# Fill in what each domain should display as when running the command in the same order as your domains
display:
  - "play"
  - "hub"
  - "fun"
  ```
