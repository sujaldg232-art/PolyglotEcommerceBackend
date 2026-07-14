package middleware

import (
	"net"
	"net/http"
	"sync"
	"time"
)

type Bucket struct {
	mu           sync.Mutex
	maxTokens    float64
	tokens       float64
	refillRate   float64
	lastRefilled time.Time
}

type IPRateLimiter struct {
	mu      sync.Mutex
	buckets map[string]*Bucket
}

var Limiter = &IPRateLimiter{
	buckets: make(map[string]*Bucket),
}

func RateLimiter(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ip, _, err := net.SplitHostPort(r.RemoteAddr)
		if err != nil {
			ip = r.RemoteAddr
		}

		Limiter.mu.Lock()
		bucket, exists := Limiter.buckets[ip]
		if !exists {
			bucket = &Bucket{
				maxTokens:    6.0,
				tokens:       2.0,
				refillRate:   1,
				lastRefilled: time.Now(),
			}
			Limiter.buckets[ip] = bucket

		}
		Limiter.mu.Unlock()

		bucket.mu.Lock()
		defer bucket.mu.Unlock()

		now := time.Now()
		duration := now.Sub(bucket.lastRefilled).Seconds()
		bucket.tokens += duration * bucket.refillRate

		if bucket.tokens > bucket.maxTokens {
			bucket.tokens = bucket.maxTokens
		}
		bucket.lastRefilled = now

		if bucket.tokens < 1.0 {
			http.Error(w, "HTTP rate limit exceeded", http.StatusTooManyRequests)
			return
		}

		bucket.tokens--
		next.ServeHTTP(w, r)
	})
}
