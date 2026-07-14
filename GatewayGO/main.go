	package main
	
	import (
		"myproject/middleware"
		"net/http"
		"net/http/httputil"
		"net/url"
		"strings"
		"time"
	)
	
	func main() {
		hostName := map[string]*url.URL{
			"userservice":    parseURL("http://user-service:8081"),
			"orderservice":   parseURL("http://order-service:8083"),
			"productservice": parseURL("http://product-service:8082"),
		}
	
		proxy := &httputil.ReverseProxy{
			Rewrite: func(pr *httputil.ProxyRequest) {
				parts := strings.Split(pr.In.URL.Path, "/")
	
				if len(parts) < 2 {
					return
				}
	
				targetURL, ok := hostName[parts[1]]
	
				if !ok {
					return
				}
	
				pr.SetURL(targetURL)
				pr.SetXForwarded()
	
				prefix := "/" + parts[1]
				pr.Out.URL.Path = strings.TrimPrefix(pr.In.URL.Path, prefix)
				pr.Out.URL.RawQuery = pr.In.URL.RawQuery
			},
		}
	
		mux := http.NewServeMux()
	
		mux.Handle("/", middleware.RateLimiter(middleware.AuthMiddleware(proxy)))
	
		srv := &http.Server{
			MaxHeaderBytes: 1 << 20,
			Addr:           ":8080",
			Handler:        mux,
			ReadTimeout:    5 * time.Second,
			WriteTimeout:   10 * time.Second,
			IdleTimeout:    15 * time.Second,
		}
	
		srv.ListenAndServe()
	}
	
	func parseURL(s string) *url.URL {
		u, _ := url.Parse(s)
		return u
	}
