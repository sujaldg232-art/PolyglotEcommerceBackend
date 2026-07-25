package middleware

import (
	"net"
	"net/http"
	"time"

	"github.com/redis/go-redis/v9"
)

var redisClient = redis.NewClient(&redis.Options{
	Addr:     "redis-server:6379",
	Password: "",
	DB:       0,
})

var rateLimitScript = redis.NewScript(`
	local key = KEYS[1]
	local max_tokens = tonumber(ARGV[1])
	local refill_rate = tonumber(ARGV[2])
	local now = tonumber(ARGV[3])
	local ttl = tonumber(ARGV[4])

	local data = redis.call("HMGET", key, "tokens", "last_refilled")
	local tokens = tonumber(data[1])
	local last_refilled = tonumber(data[2])

	if not tokens then
		tokens = max_tokens
		last_refilled = now
	else
		local elapsed = now - last_refilled
		tokens = math.min(max_tokens, tokens + elapsed * refill_rate)
	end

	if tokens < 1.0 then
		redis.call("HSET", key, "tokens", tokens)
		redis.call("EXPIRE", key, ttl)
		return 0
	else
		tokens = tokens - 1.0
		redis.call("HSET", key, "tokens", tokens, "last_refilled", now)
		redis.call("EXPIRE", key, ttl)
		return 1
	end
`)

func RateLimiter(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ip, _, err := net.SplitHostPort(r.RemoteAddr)
		if err != nil {
			ip = r.RemoteAddr
		}

		ctx := r.Context()
		key := "rate_limit:" + ip

		maxTokens := 6.0
		refillRate := 1.0
		now := time.Now().Unix()
		ttl := 3600

		allowed, err := rateLimitScript.Run(ctx, redisClient, []string{key}, maxTokens, refillRate, now, ttl).Int()
		if err != nil {
			http.Error(w, "Internal Server Error", http.StatusInternalServerError)
			return
		}

		if allowed == 0 {
			http.Error(w, "HTTP rate limit exceeded", http.StatusTooManyRequests)
			return
		}

		next.ServeHTTP(w, r)
	})
}
