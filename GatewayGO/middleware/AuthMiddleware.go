package middleware

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strings"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
)

var jwtKey = []byte("CGeOyQCzO4qyiqyxOzRC+RbLXutHk9qlCTuh4Nr1aDPftNI3QAq7+ARsbcRQS/nX52tUshS5+MCPpIPfGj3vYA==")

var excludedPaths = map[string]bool{
	"/userservice/Registration":      true,
	"/userservice/login":             true,
	"/userservice/PasswordResetstg1": true,
	"/userservice/PasswordResetstg2": true,
	"/userservice/PasswordResetstg3": true,
	"/userservice/refresh":           true,
}

type CustomClaims struct {
	Id        string `json:"id"`
	Role      string `json:"role"`
	IsActive  bool   `json:"isActive"`
	IsDeleted bool   `json:"isDeleted"`
	jwt.RegisteredClaims
}

var ctx = context.Background()

func AuthMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if excludedPaths[r.URL.Path] {
			next.ServeHTTP(w, r)
			return
		}

		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			http.Error(w, "Unauthorized1", http.StatusUnauthorized)
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			http.Error(w, "Unauthorized2", http.StatusUnauthorized)
			return
		}

		tokenStr := parts[1]
		claims := &CustomClaims{}

		UserRole := claims.Role

		if UserRole == "BUYER" && strings.HasPrefix(r.URL.Path, "/productservice") {
			http.Error(w, "ACCESS DENIED : BUYER CAN NOT ACCESS PRODUCT ENDPOINTS!!", http.StatusForbidden)
			return
		}

		if UserRole == "SELLER" && strings.HasPrefix(r.URL.Path, "/orderservice") {
			http.Error(w, "ACCESS DENIED : SELLER CAN NOT ACCESS ORDER ENDPOINTS!!", http.StatusForbidden)
			return
		}

		rdb := redis.NewClient(&redis.Options{
			Addr:     "redis-server:6379",
			Password: "",
			DB:       0,
		})

		_, err := rdb.Get(ctx, tokenStr).Result()

		if err == nil {
			w.Header().Set("WWW-Authenticate", `Bearer error="invalid_token", error_description="The token has been revoked"`)
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte(`{"code": "TOKEN_REVOKED", "message": "This session has been invalidated or logged out"}`))
			return
		} else if err != redis.Nil {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte(`{"code": "SERVER_ERROR", "message": "Internal authentication service error"}`))
			return
		}

		token, err := jwt.ParseWithClaims(tokenStr, claims, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method")
			}
			return jwtKey, nil
		})

		if err != nil {
			if errors.Is(err, jwt.ErrTokenExpired) {
				w.Header().Set("WWW-Authenticate", `Bearer error="invalid_token", error_description="The access token expired"`)
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusUnauthorized)
				w.Write([]byte(`{"code": "TOKEN_EXPIRED", "message": "Token has expired"}`))
				return
			}

			http.Error(w, "Unauthorized3", http.StatusUnauthorized)
			return
		}

		if !token.Valid {
			http.Error(w, "Unauthorized4", http.StatusUnauthorized)
			return
		}

		if claims.Id == "" {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte(`{"error": "invalid_payload", "message": "User ID claim is missing or empty"}`))
			return
		}

		_, err = uuid.Parse(claims.Id)
		if err != nil {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusBadRequest)
			w.Write([]byte(fmt.Sprintf(`{"error": "invalid_uuid", "message": "User ID is not a valid UUID", "details": "%s"}`, err.Error())))
			return
		}

		isActive := claims.IsActive
		isDeleted := claims.IsDeleted
		if !isActive || isDeleted {
			http.Error(w, "Unauthorized Account Is Not Active Or is to be Deleted!", http.StatusUnauthorized)
			return
		}

		r.Header.Set("X-User-Id", claims.Id)
		r.Header.Set("X-User-Role", UserRole)

		next.ServeHTTP(w, r)
	})
}
