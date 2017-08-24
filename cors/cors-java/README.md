# CORS recipe for Lagom's Javadsl


```
curl -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: origin, x-requested-with" \
        -H "Origin: http://www.some-domain.com"  \
        -X OPTIONS http://localhost:9000/api/hello/123 -v        
```
