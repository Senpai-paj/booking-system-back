# DTO Schemas

## Auth DTOs

### RegisterRequest
```json
{
  "email": "user@example.com",
  "password": "secret123",
  "firstName": "Ivan",
  "lastName": "Petrov",
  "role": "CLIENT"
}
```

### LoginRequest
```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

### LoginResponse
```json
{
"accessToken": "jwt-token-here"
}
```

## Employee DTOs
### EmployeeResponse
```json
{
"id": 1,
"fullName": "Ivan Petrov",
"specialization": "Hairdresser"
}
```

## Service DTOs
### ServiceResponse
```json
{
"id": 1,
"name": "Haircut",
"durationMinutes": 30
}
```

## Booking DTOs
### CreateBookingRequest
```json
{
"employeeId": 1,
"serviceId": 2,
"startTime": "2026-07-01T10:00:00"
}
```

### BookingResponse
```json
{
"id": 1,
"employeeName": "Ivan Petrov",
"serviceName": "Haircut",
"startTime": "2026-07-01T10:00:00",
"endTime": "2026-07-01T10:30:00",
"status": "PENDING"
}
```

# Booking  Statuses
- PENDING — Awaiting confirmation
- CONFIRMED  — Confirmed by employee/admin
- COMPLETED — Service has been provided
- CANCELLED — Booking was cancelled