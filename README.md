# autocdn

This project aims at enabling automated/simplified loading of static resources into a CDN. This would enable better performance (netword+costs) of you website/webapp.

## Features

- Automated upload of resources in some CDN, through a plain `GET` request
- Invalidation of CDN content through a `DELETE` request

## Not-features

- Resources may not be stored indefinitely in the CDN, typically as a free-plan would have limited size. Hence, original content should remain available as long as needed.