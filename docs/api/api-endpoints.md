# API Endpoints

##  Auth
| Method | Endpoint | Description  |
|--------|----------|--------------|
| POST | `/auth/register` | Registration |
| POST | `/auth/login` | Login        |

##  Employees
| Method | Endpoint | Description |
|--------|----------|---|
| GET | `/employees` | List of employees |
| GET | `/employees/{id}` | Employee Information |

##  Services
| Method | Endpoint | Description |
|--------|----------|----------|
| GET | `/services` | List of services |
| GET | `/services/{id}` | Information about the service |

##  Schedule
| Method | Endpoint | Description |
|--------|----------|----------|
| GET | `/employees/{id}/available-slots` | Free slots |

**Parameters:**
- `date` (YYYY-MM-DD) — date
- `serviceId` — Service ID

##  Booking
| Method | Endpoint | Description      |
|--------|----------|------------------|
| POST | `/bookings` | Create a booking |
| GET | `/bookings/my` | My booking       |
| PATCH | `/bookings/{id}/cancel` | cancel booking   |

##  Admin (Need a role ADMIN)
| Method | Endpoint | Description       |
|--------|----------|-------------------|
| POST | `/admin/services` | Create a service  |
| PUT | `/admin/services/{id}` | Update service    |
| POST | `/admin/schedules` | Create a schedule |
| PATCH | `/admin/employees/{id}/approve` | Confirm employee  |