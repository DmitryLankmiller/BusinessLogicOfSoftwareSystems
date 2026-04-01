export BASE_URL="http://localhost:8080/api"

ADMIN_LOGIN="admin"
HOST_LOGIN="host1"
USER_LOGIN="user1"

ADMIN_ROLE="ADMIN"
HOST_ROLE="HOST"
USER_ROLE="USER"

export ADMIN_HEADERS=(-H "X-User-Login: $ADMIN_LOGIN" -H "X-User-Role: $ADMIN_ROLE")
export HOST_HEADERS=(-H "X-User-Login: $HOST_LOGIN" -H "X-User-Role: $HOST_ROLE")
export USER_HEADERS=(-H "X-User-Login: $USER_LOGIN" -H "X-User-Role: $USER_ROLE")
export JSON_HEADER=(-H "Content-Type: application/json")

curl -X GET "$BASE_URL/users?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/users/1" \
  "${ADMIN_HEADERS[@]}"

curl -X POST "$BASE_URL/users" \
  "${ADMIN_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "login": "user2",
    "name": "User Two",
    "email": "user2@mail.ru"
  }'

curl -X DELETE "$BASE_URL/users/5" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/accommodations?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${HOST_HEADERS[@]}"

curl -X GET "$BASE_URL/accommodations?check_in=2026-05-10&check_out=2026-05-15&guests_count=2&page=0&size=10&sort_by=price_per_night&sort_dir=asc" \
  "${USER_HEADERS[@]}"

curl -X GET "$BASE_URL/accommodations/1" \
  "${HOST_HEADERS[@]}"

curl -X POST "$BASE_URL/accommodations" \
  "${HOST_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "host_id": 2,
    "name": "Beach House",
    "description": "Nice house near the sea",
    "max_guests_number": 4,
    "beds_count": 2,
    "address": "Saint-Petersburg, Nevskiy prospekt, 100",
    "rating": 4.7,
    "price_per_night": 150000,
    "published": true
  }'

curl -X PUT "$BASE_URL/accommodations/1" \
  "${HOST_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "host_id": 2,
    "name": "Beach House Updated",
    "description": "Updated description",
    "max_guests_number": 5,
    "beds_count": 3,
    "address": "Saint-Petersburg, Nevskiy prospekt, 100",
    "rating": 4.9,
    "price_per_night": 170000,
    "published": true
  }'

curl -X DELETE "$BASE_URL/accommodations/3" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/bookings?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/bookings?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${USER_HEADERS[@]}"

curl -X GET "$BASE_URL/bookings?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${HOST_HEADERS[@]}"

curl -X GET "$BASE_URL/bookings?accommodation=1&page=0&size=10&sort_by=check_in&sort_dir=asc" \
  "${HOST_HEADERS[@]}"


curl -X GET "$BASE_URL/bookings/1" \
  "${USER_HEADERS[@]}"

curl -X POST "$BASE_URL/bookings" \
  "${ADMIN_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "accommodation_id": 1,
    "user_id": 3,
    "check_in": "2026-06-01",
    "check_out": "2026-06-05",
    "price": 600000
  }'

curl -X PUT "$BASE_URL/bookings/1" \
  "${ADMIN_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "accommodation_id": 1,
    "user_id": 3,
    "check_in": "2026-06-02",
    "check_out": "2026-06-06",
    "price": 620000
  }'

curl -X DELETE "$BASE_URL/bookings/2" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/booking-requests?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${ADMIN_HEADERS[@]}"

curl -X GET "$BASE_URL/booking-requests?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${USER_HEADERS[@]}"

curl -X GET "$BASE_URL/booking-requests?page=0&size=10&sort_by=id&sort_dir=asc" \
  "${HOST_HEADERS[@]}"

curl -X GET "$BASE_URL/booking-requests?accommodation=1&page=0&size=10&sort_by=check_in&sort_dir=asc" \
  "${HOST_HEADERS[@]}"

curl -X GET "$BASE_URL/booking-requests/1" \
  "${USER_HEADERS[@]}"

curl -X POST "$BASE_URL/booking-requests" \
  "${USER_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "accommodation_id": 1,
    "client_id": 3,
    "check_in": "2026-07-10",
    "check_out": "2026-07-15",
    "message_to_host": "Hello, Host!",
    "payment_input_info": {
      "payment_method": "yookassa",
      "payment_method_input_info": {
        "csc": "213",
        "expiry_month": "01",
        "expiry_year": "2030",
        "number": "2202474301322987"
      }
    }
  }'

curl -X POST "$BASE_URL/booking-requests/7/resolved" \
  "${HOST_HEADERS[@]}" \
  "${JSON_HEADER[@]}" \
  -d '{
    "confirm": true,
    "reason": null
  }'