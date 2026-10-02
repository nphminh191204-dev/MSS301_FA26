const fs = require('fs');
const path = require('path');

const env = {
  id: "e4d754b2-2936-4d2a-8ef7-83d8a7c2e391",
  name: "FUCinema-Local",
  values: [
    { key: "gateway", value: "http://localhost:9000", type: "default", enabled: true },
    { key: "adminToken", value: "", type: "default", enabled: true },
    { key: "customerToken", value: "", type: "default", enabled: true },
    { key: "customer2Token", value: "", type: "default", enabled: true },
    { key: "newEmail", value: "", type: "default", enabled: true },
    { key: "newCustomerId", value: "", type: "default", enabled: true },
    { key: "adminCreatedId", value: "", type: "default", enabled: true },
    { key: "adminEmail2", value: "", type: "default", enabled: true },
    { key: "genreId", value: "", type: "default", enabled: true },
    { key: "roomId", value: "", type: "default", enabled: true },
    { key: "movieId", value: "", type: "default", enabled: true },
    { key: "futureDate", value: "", type: "default", enabled: true },
    { key: "futureStart", value: "", type: "default", enabled: true },
    { key: "futureStartOverlap", value: "", type: "default", enabled: true },
    { key: "showtimeId", value: "", type: "default", enabled: true },
    { key: "showtime2Id", value: "", type: "default", enabled: true },
    { key: "bookingId", value: "", type: "default", enabled: true },
    { key: "booking2Id", value: "", type: "default", enabled: true },
    { key: "booking3Id", value: "", type: "default", enabled: true },
    { key: "today", value: "", type: "default", enabled: true },
    { key: "notFoundId", value: "66f9ffffffffffffffffffff", type: "default", enabled: true },
    { key: "seedGenreActionId", value: "66f000000000000000000001", type: "default", enabled: true },
    { key: "seedGenreScifiId", value: "66f000000000000000000005", type: "default", enabled: true },
    { key: "seedRoom2Id", value: "66f100000000000000000002", type: "default", enabled: true },
    { key: "seedRoomMaintenanceId", value: "66f100000000000000000004", type: "default", enabled: true },
    { key: "seedMovieEndedId", value: "66f200000000000000000004", type: "default", enabled: true }
  ],
  _postman_variable_scope: "environment"
};

function req(name, method, urlPath, authType, bodyObj, testScript, preScript, extraHeaders) {
  const item = {
    name: name,
    request: {
      method: method,
      header: extraHeaders || [],
      url: {
        raw: "{{gateway}}" + urlPath,
        host: ["{{gateway}}"],
        path: urlPath.split('/').filter(Boolean)
      }
    },
    event: []
  };

  if (authType === "adminToken" || authType === "customerToken" || authType === "customer2Token") {
    item.request.auth = {
      type: "bearer",
      bearer: [{ key: "token", value: `{{${authType}}}`, type: "string" }]
    };
  } else if (authType === "fakeToken") {
    item.request.auth = {
      type: "bearer",
      bearer: [{ key: "token", value: "abc.def.ghi", type: "string" }]
    };
  }

  if (bodyObj !== null && bodyObj !== undefined) {
    item.request.header.push({ key: "Content-Type", value: "application/json", type: "text" });
    item.request.body = {
      mode: "raw",
      raw: typeof bodyObj === "string" ? bodyObj : JSON.stringify(bodyObj, null, 2)
    };
  }

  if (preScript) {
    item.event.push({
      listen: "prerequest",
      script: {
        type: "text/javascript",
        exec: Array.isArray(preScript) ? preScript : preScript.split('\n')
      }
    });
  }

  if (testScript) {
    item.event.push({
      listen: "test",
      script: {
        type: "text/javascript",
        exec: Array.isArray(testScript) ? testScript : testScript.split('\n')
      }
    });
  }

  return item;
}

const errCheck = (code) => `
pm.test("Status ${code}", () => pm.response.to.have.status(${code}));
const err = pm.response.json();
pm.test("Error body format", () => {
    pm.expect(err).to.have.all.keys("timestamp", "status", "error", "message", "path");
    pm.expect(err.status).to.eql(pm.response.code);
});
`;

// Folder 01 - Auth
const folder01 = {
  name: "01-Auth",
  item: [
    req("1.1 Login Admin", "POST", "/api/auth/login", null,
      { email: "admin@fucinema.com", password: "@@abc123@@" },
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test("Role is ADMIN", () => pm.expect(json.role).to.eql("ADMIN"));
pm.test("Has Bearer token", () => {
    pm.expect(json.tokenType).to.eql("Bearer");
    pm.expect(json.accessToken.split(".")).to.have.lengthOf(3);
});
pm.environment.set("adminToken", json.accessToken);`
    ),
    req("1.2 Login Customer", "POST", "/api/auth/login", null,
      { email: "an@gmail.com", password: "123456" },
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test("Role is CUSTOMER", () => pm.expect(json.role).to.eql("CUSTOMER"));
pm.test("userId = 1", () => pm.expect(json.userId).to.eql(1));
pm.environment.set("customerToken", json.accessToken);`
    ),
    req("1.3 Login Customer - Sai mat khau", "POST", "/api/auth/login", null,
      { email: "an@gmail.com", password: "sai-mat-khau" },
      errCheck(401)
    ),
    req("1.4 Login Customer - INACTIVE", "POST", "/api/auth/login", null,
      { email: "chi@gmail.com", password: "123456" },
      errCheck(403)
    ),
    req("1.5 Login Customer - Email khong hop le", "POST", "/api/auth/login", null,
      { email: "abc", password: "" },
      errCheck(400)
    ),
    req("1.6 Get Profile - Khong token", "GET", "/api/customers/me", null, null,
      `pm.test("Status 401", () => pm.response.to.have.status(401));`
    ),
    req("1.7 Get Profile - Token sai chu ky", "GET", "/api/customers/me", "fakeToken", null,
      `pm.test("Status 401", () => pm.response.to.have.status(401));`
    )
  ]
};

// Folder 02 - Customer
const folder02 = {
  name: "02-Customer",
  item: [
    req("2.1 Customer dang ky thanh cong", "POST", "/api/customers/register", null,
      `{
  "customerName": "Phạm Thị Dung",
  "telephone": "0987654321",
  "email": "{{newEmail}}",
  "customerBirthday": "2004-03-15",
  "password": "123456"
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.test("Status ACTIVE", () => pm.expect(json.customerStatus).to.eql("ACTIVE"));
pm.test("Password is NOT returned", () => pm.expect(json).to.not.have.property("password"));
pm.environment.set("newCustomerId", json.customerId);`,
      `pm.environment.set("newEmail", \`user\${Date.now()}@gmail.com\`);`
    ),
    req("2.2 Customer dang ky - Email da ton tai", "POST", "/api/customers/register", null,
      `{
  "customerName": "Phạm Thị Dung",
  "telephone": "0987654321",
  "email": "an@gmail.com",
  "customerBirthday": "2004-03-15",
  "password": "123456"
}`,
      errCheck(409)
    ),
    req("2.3 Customer dang ky - Validation failed", "POST", "/api/customers/register", null,
      { customerName: "", telephone: "123", email: "x", customerBirthday: "2099-01-01", password: "1" },
      `pm.test("Status 400", () => pm.response.to.have.status(400));
const msg = pm.response.json().message;
["customerName", "telephone", "email", "customerBirthday", "password"]
    .forEach(f => pm.test(\`Message mentions \${f}\`, () => pm.expect(msg).to.include(f)));`
    ),
    req("2.4 Login customer vua dang ky", "POST", "/api/auth/login", null,
      `{ "email": "{{newEmail}}", "password": "123456" }`,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.environment.set("customer2Token", pm.response.json().accessToken);`
    ),
    req("2.5 Xem profile customer 1", "GET", "/api/customers/me", "customerToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test("Email is an@gmail.com", () => pm.expect(json.email).to.eql("an@gmail.com"));`
    ),
    req("2.6 Cap nhat profile customer 1", "PUT", "/api/customers/me", "customerToken",
      { customerName: "Nguyễn Văn An (updated)", telephone: "0905999999", customerBirthday: "2002-05-10" },
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test("Profile updated", () => {
    pm.expect(json.customerName).to.eql("Nguyễn Văn An (updated)");
    pm.expect(json.telephone).to.eql("0905999999");
});`
    ),
    req("2.7 Doi mat khau - Sai mat khau cu", "PUT", "/api/customers/me/password", "customer2Token",
      { oldPassword: "sai", newPassword: "654321" },
      errCheck(400)
    ),
    req("2.8 Doi mat khau thanh cong", "PUT", "/api/customers/me/password", "customer2Token",
      { oldPassword: "123456", newPassword: "654321" },
      `pm.test("Status 204", () => pm.response.to.have.status(204));`
    ),
    req("2.9 Login lai bang mat khau moi", "POST", "/api/auth/login", null,
      `{ "email": "{{newEmail}}", "password": "654321" }`,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.environment.set("customer2Token", pm.response.json().accessToken);`
    ),
    req("2.10 Customer xem danh sach customer - 403", "GET", "/api/customers", "customerToken", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("2.11 Admin tim kiem customer", "GET", "/api/customers?keyword=gmail", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const list = pm.response.json();
pm.test("At least 3 customers", () => pm.expect(list.length).to.be.at.least(3));
pm.test("No password in list", () => list.forEach(c => pm.expect(c).to.not.have.property("password")));`
    ),
    req("2.12 Admin get customer khong ton tai - 404", "GET", "/api/customers/99999", "adminToken", null,
      errCheck(404)
    ),
    req("2.13 Admin tao customer", "POST", "/api/customers", "adminToken",
      `{
  "customerName": "Khách hàng do Admin tạo",
  "telephone": "0911222333",
  "email": "{{adminEmail2}}",
  "customerBirthday": "1999-09-09",
  "customerStatus": "ACTIVE",
  "password": "123456"
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
pm.environment.set("adminCreatedId", pm.response.json().customerId);`,
      `pm.environment.set("adminEmail2", \`staff\${Date.now()}@gmail.com\`);`
    ),
    req("2.14 Admin cap nhat customer", "PUT", "/api/customers/{{adminCreatedId}}", "adminToken",
      `{
  "customerName": "Khách hàng do Admin tạo (updated)",
  "telephone": "0911222333",
  "email": "{{adminEmail2}}",
  "customerBirthday": "1999-09-09",
  "customerStatus": "ACTIVE"
}`,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Name updated", () => pm.expect(pm.response.json().customerName).to.include("(updated)"));`
    ),
    req("2.15 Admin xoa mem customer", "DELETE", "/api/customers/{{adminCreatedId}}", "adminToken", null,
      `pm.test("Status 204", () => pm.response.to.have.status(204));`
    ),
    req("2.16 Kiem tra customer da xoa mem", "GET", "/api/customers/{{adminCreatedId}}", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Soft deleted -> INACTIVE", () => pm.expect(pm.response.json().customerStatus).to.eql("INACTIVE"));`
    ),
    req("2.17 Admin xem profile /me - 403", "GET", "/api/customers/me", "adminToken", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("2.18 Gateway chan spoofing X-User-Id", "GET", "/api/customers/me", "customerToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Gateway overrides spoofed X-User-Id", () => pm.expect(pm.response.json().email).to.eql("an@gmail.com"));`,
      null,
      [{ key: "X-User-Id", value: "2", type: "text" }]
    )
  ]
};

// Folder 03 - Genre & Room
const folder03 = {
  name: "03-Genre-Room",
  item: [
    req("3.1 Public xem danh sach the loai", "GET", "/api/genres", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("At least 5 genres", () => pm.expect(pm.response.json().length).to.be.at.least(5));`
    ),
    req("3.2 Admin tao the loai", "POST", "/api/genres", "adminToken",
      `{ "genreName": "Tài liệu {{$timestamp}}", "description": "Phim tài liệu" }`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
pm.environment.set("genreId", pm.response.json().genreId);`
    ),
    req("3.3 Customer tao the loai - 403", "POST", "/api/genres", "customerToken",
      `{ "genreName": "Tài liệu {{$timestamp}}", "description": "Phim tài liệu" }`,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("3.4 Admin tao the loai trung ten - 409", "POST", "/api/genres", "adminToken",
      { genreName: "Hành động" },
      errCheck(409)
    ),
    req("3.5 Admin sua the loai", "PUT", "/api/genres/{{genreId}}", "adminToken",
      `{ "genreName": "Tài liệu {{$timestamp}}", "description": "Đã cập nhật" }`,
      `pm.test("Status 200", () => pm.response.to.have.status(200));`
    ),
    req("3.6 Admin xoa the loai dang co phim - 409", "DELETE", "/api/genres/{{seedGenreActionId}}", "adminToken", null,
      errCheck(409)
    ),
    req("3.7 Admin tao phong chieu", "POST", "/api/rooms", "adminToken",
      `{ "roomName": "Room Test {{$timestamp}}", "roomType": "STANDARD", "seatRows": 5, "seatsPerRow": 8, "roomStatus": "ACTIVE" }`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
const room = pm.response.json();
pm.test("totalSeats = seatRows x seatsPerRow", () => pm.expect(room.totalSeats).to.eql(40));
pm.environment.set("roomId", room.roomId);`
    ),
    req("3.8 Public xem phong chieu - 401", "GET", "/api/rooms", null, null,
      `pm.test("Status 401", () => pm.response.to.have.status(401));`
    ),
    req("3.9 Admin tao phong - Sai RoomType - 400", "POST", "/api/rooms", "adminToken",
      { roomName: "X", roomType: "VIP", seatRows: 5, seatsPerRow: 8, roomStatus: "ACTIVE" },
      errCheck(400)
    ),
    req("3.10 Admin tao phong - Sai seatRows - 400", "POST", "/api/rooms", "adminToken",
      { roomName: "Y", roomType: "IMAX", seatRows: 0, seatsPerRow: 8, roomStatus: "ACTIVE" },
      errCheck(400)
    )
  ]
};

// Folder 04 - Movie
const folder04 = {
  name: "04-Movie",
  item: [
    req("4.1 Admin tao phim", "POST", "/api/movies", "adminToken",
      `{
  "title": "Hành Trình Phương Nam",
  "description": "Phim tài liệu về miền Tây sông nước.",
  "director": "Lê Hoàng",
  "durationMinutes": 120,
  "language": "Tiếng Việt",
  "ageRating": "P",
  "releaseDate": "2026-10-01",
  "genreId": "{{genreId}}",
  "movieStatus": "NOW_SHOWING"
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
const m = pm.response.json();
pm.test("Genre mapped", () => pm.expect(m.genreId).to.eql(pm.environment.get("genreId")));
pm.test("movieId is ObjectId", () => pm.expect(m.movieId).to.match(/^[0-9a-f]{24}$/));
pm.environment.set("movieId", m.movieId);`
    ),
    req("4.2 Public tim phim theo keyword", "GET", "/api/movies?keyword=galaxy", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Contains Galaxy Rangers", () => {
    pm.expect(pm.response.json().some(m => m.title.includes("Galaxy Rangers"))).to.be.true;
});`
    ),
    req("4.3 Public tim phim theo genre va status", "GET", "/api/movies?status=NOW_SHOWING&genreId={{seedGenreScifiId}}", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.response.json().forEach(m => pm.test(\`Movie \${m.movieId} matches filter\`, () => {
    pm.expect(m.movieStatus).to.eql("NOW_SHOWING");
    pm.expect(m.genreId).to.eql(pm.environment.get("seedGenreScifiId"));
}));`
    ),
    req("4.4 Admin sua phim", "PUT", "/api/movies/{{movieId}}", "adminToken",
      `{
  "title": "Hành Trình Phương Nam",
  "description": "Phim tài liệu về miền Tây sông nước.",
  "director": "Lê Hoàng",
  "durationMinutes": 120,
  "language": "Tiếng Việt (lồng tiếng)",
  "ageRating": "P",
  "releaseDate": "2026-10-01",
  "genreId": "{{genreId}}",
  "movieStatus": "NOW_SHOWING"
}`,
      `pm.test("Status 200", () => pm.response.to.have.status(200));`
    ),
    req("4.5 Public xem phim khong ton tai - 404", "GET", "/api/movies/{{notFoundId}}", null, null,
      errCheck(404)
    ),
    req("4.6 Admin tao phim - Sai duration - 400", "POST", "/api/movies", "adminToken",
      `{
  "title": "Phim ngan",
  "durationMinutes": 10,
  "ageRating": "P",
  "genreId": "{{genreId}}",
  "movieStatus": "NOW_SHOWING"
}`,
      errCheck(400)
    ),
    req("4.7 Admin tao phim - Genre khong ton tai - 404", "POST", "/api/movies", "adminToken",
      `{
  "title": "Phim Test BR15",
  "durationMinutes": 90,
  "ageRating": "P",
  "genreId": "{{notFoundId}}",
  "movieStatus": "NOW_SHOWING"
}`,
      errCheck(404)
    ),
    req("4.8 Xoa the loai dang co phim - 409", "DELETE", "/api/genres/{{genreId}}", "adminToken", null,
      errCheck(409)
    )
  ]
};

// Folder 05 - Showtime
const folder05 = {
  name: "05-Showtime",
  item: [
    req("5.1 Admin tao suat chieu", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{movieId}}",
  "roomId": "{{roomId}}",
  "startTime": "{{futureStart}}",
  "ticketPrice": 95000
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
const s = pm.response.json();
pm.test("endTime = startTime + duration (120')", () => {
    const minutes = (new Date(s.endTime) - new Date(s.startTime)) / 60000;
    pm.expect(minutes).to.eql(120);
});
pm.test("Status SCHEDULED", () => pm.expect(s.showtimeStatus).to.eql("SCHEDULED"));
pm.environment.set("showtimeId", s.showtimeId);`,
      `const pad = n => String(n).padStart(2, "0");
const d = new Date();
d.setDate(d.getDate() + 7);
const day = \`\${d.getFullYear()}-\${pad(d.getMonth() + 1)}-\${pad(d.getDate())}\`;
pm.environment.set("futureDate", day);
pm.environment.set("futureStart", \`\${day}T19:00:00\`);
pm.environment.set("futureStartOverlap", \`\${day}T20:00:00\`);`
    ),
    req("5.2 Admin tao suat chieu trung gio cung phong - 409", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{movieId}}",
  "roomId": "{{roomId}}",
  "startTime": "{{futureStartOverlap}}",
  "ticketPrice": 95000
}`,
      errCheck(409)
    ),
    req("5.3 Admin tao suat chieu cung gio khac phong - OK", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{movieId}}",
  "roomId": "{{seedRoom2Id}}",
  "startTime": "{{futureStart}}",
  "ticketPrice": 95000
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
pm.environment.set("showtime2Id", pm.response.json().showtimeId);`
    ),
    req("5.4 Admin tao suat chieu - Phim ENDED - 400", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{seedMovieEndedId}}",
  "roomId": "{{roomId}}",
  "startTime": "{{futureStart}}",
  "ticketPrice": 95000
}`,
      errCheck(400)
    ),
    req("5.5 Admin tao suat chieu - Phong MAINTENANCE - 400", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{movieId}}",
  "roomId": "{{seedRoomMaintenanceId}}",
  "startTime": "{{futureStart}}",
  "ticketPrice": 95000
}`,
      errCheck(400)
    ),
    req("5.6 Admin tao suat chieu - Gio qua khu - 400", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{movieId}}",
  "roomId": "{{roomId}}",
  "startTime": "2020-01-01T10:00:00",
  "ticketPrice": 95000
}`,
      errCheck(400)
    ),
    req("5.7 Public loc lich chieu theo movieId va date", "GET", "/api/showtimes?movieId={{movieId}}&date={{futureDate}}", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const list = pm.response.json();
pm.test("Filter by movie & date", () => {
    pm.expect(list.length).to.be.at.least(1);
    list.forEach(s => {
        pm.expect(s.movieId).to.eql(pm.environment.get("movieId"));
        pm.expect(s.startTime.substring(0, 10)).to.eql(pm.environment.get("futureDate"));
    });
});`
    ),
    req("5.8 Public xem chi tiet suat chieu", "GET", "/api/showtimes/{{showtimeId}}", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const s = pm.response.json();
pm.test("Seat rows and per row exist", () => {
    pm.expect(s.seatRows).to.eql(5);
    pm.expect(s.seatsPerRow).to.eql(8);
});`
    ),
    req("5.9 Admin huy suat chieu 2", "DELETE", "/api/showtimes/{{showtime2Id}}", "adminToken", null,
      `pm.test("Status 204", () => pm.response.to.have.status(204));`
    ),
    req("5.10 Kiem tra suat chieu da chuyen CANCELLED", "GET", "/api/showtimes/{{showtime2Id}}", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Status CANCELLED", () => pm.expect(pm.response.json().showtimeStatus).to.eql("CANCELLED"));`
    ),
    req("5.11 Admin xoa phong dang co suat chieu - 409", "DELETE", "/api/rooms/{{roomId}}", "adminToken", null,
      errCheck(409)
    ),
    req("5.12 Admin xoa phim dang co suat chieu - 409", "DELETE", "/api/movies/{{movieId}}", "adminToken", null,
      errCheck(409)
    ),
    req("5.13 Admin tao suat chieu - Phim khong ton tai - 404", "POST", "/api/showtimes", "adminToken",
      `{
  "movieId": "{{notFoundId}}",
  "roomId": "{{roomId}}",
  "startTime": "{{futureStart}}",
  "ticketPrice": 95000
}`,
      errCheck(404)
    )
  ]
};

// Folder 06 - Booking
const folder06 = {
  name: "06-Booking",
  item: [
    req("6.1 Public xem so do ghe chua co ai dat", "GET", "/api/bookings/showtimes/{{showtimeId}}/seats", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const m = pm.response.json();
pm.test("40 seats, all available", () => {
    pm.expect(m.totalSeats).to.eql(40);
    pm.expect(m.availableSeats).to.eql(40);
    pm.expect(m.bookedSeats).to.be.empty;
});`
    ),
    req("6.2 Customer 1 dat 2 ve E5, E6", "POST", "/api/bookings", "customerToken",
      `{
  "items": [
    { "showtimeId": "{{showtimeId}}", "seatCode": "E5" },
    { "showtimeId": "{{showtimeId}}", "seatCode": "E6" }
  ]
}`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
const b = pm.response.json();
pm.test("CONFIRMED with 2 tickets", () => {
    pm.expect(b.bookingStatus).to.eql("CONFIRMED");
    pm.expect(b.details).to.have.lengthOf(2);
});
pm.test("totalPrice = sum of ticket prices (server-side)", () => {
    const sum = b.details.reduce((acc, d) => acc + Number(d.price), 0);
    pm.expect(Number(b.totalPrice)).to.eql(sum);
    pm.expect(Number(b.totalPrice)).to.eql(190000);
});
pm.test("Snapshot movie info", () => pm.expect(b.details[0].movieTitle).to.eql("Hành Trình Phương Nam"));
pm.environment.set("bookingId", b.bookingId);`
    ),
    req("6.3 Customer 2 dat lai ghe E5 - 409", "POST", "/api/bookings", "customer2Token",
      `{
  "items": [
    { "showtimeId": "{{showtimeId}}", "seatCode": "E5" }
  ]
}`,
      `pm.test("Status 409 - seat taken", () => pm.response.to.have.status(409));
pm.test("Message mentions seat", () => pm.expect(pm.response.json().message).to.include("E5"));`
    ),
    req("6.4 Customer 2 dat ghe vuot cot - 400", "POST", "/api/bookings", "customer2Token",
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "E9" }] }`,
      errCheck(400)
    ),
    req("6.5 Customer 2 dat ghe vuot hang - 400", "POST", "/api/bookings", "customer2Token",
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "F1" }] }`,
      errCheck(400)
    ),
    req("6.6 Customer 2 dat trung ghe trong cung request - 400", "POST", "/api/bookings", "customer2Token",
      `{ "items": [
  { "showtimeId": "{{showtimeId}}", "seatCode": "A2" },
  { "showtimeId": "{{showtimeId}}", "seatCode": "A2" }
]}`,
      errCheck(400)
    ),
    req("6.7 Dat ve suat chieu khong ton tai - 404", "POST", "/api/bookings", "customer2Token",
      `{ "items": [{ "showtimeId": "{{notFoundId}}", "seatCode": "A1" }] }`,
      errCheck(404)
    ),
    req("6.8 Dat ve suat chieu da huy - 400", "POST", "/api/bookings", "customer2Token",
      `{ "items": [{ "showtimeId": "{{showtime2Id}}", "seatCode": "A1" }] }`,
      errCheck(400)
    ),
    req("6.9 Dat qua 8 ve trong 1 booking - 400", "POST", "/api/bookings", "customer2Token",
      `{
  "items": [
    {"showtimeId": "{{showtimeId}}", "seatCode": "A1"}, {"showtimeId": "{{showtimeId}}", "seatCode": "A2"},
    {"showtimeId": "{{showtimeId}}", "seatCode": "A3"}, {"showtimeId": "{{showtimeId}}", "seatCode": "A4"},
    {"showtimeId": "{{showtimeId}}", "seatCode": "A5"}, {"showtimeId": "{{showtimeId}}", "seatCode": "A6"},
    {"showtimeId": "{{showtimeId}}", "seatCode": "A7"}, {"showtimeId": "{{showtimeId}}", "seatCode": "A8"},
    {"showtimeId": "{{showtimeId}}", "seatCode": "B1"}
  ]
}`,
      errCheck(400)
    ),
    req("6.10 Dat 0 ve - 400", "POST", "/api/bookings", "customer2Token",
      `{ "items": [] }`,
      errCheck(400)
    ),
    req("6.11 Admin dat ve - 403", "POST", "/api/bookings", "adminToken",
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "A1" }] }`,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("6.12 Dat ve khong token - 401", "POST", "/api/bookings", null,
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "A1" }] }`,
      `pm.test("Status 401", () => pm.response.to.have.status(401));`
    ),
    req("6.13 Customer 2 dat ve thanh cong ghe A1", "POST", "/api/bookings", "customer2Token",
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "A1" }] }`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
pm.environment.set("booking2Id", pm.response.json().bookingId);`
    ),
    req("6.14 Public kiem tra so do ghe da cap nhat", "GET", "/api/bookings/showtimes/{{showtimeId}}/seats", null, null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const m = pm.response.json();
pm.test("Booked seats updated", () => {
    pm.expect(m.bookedSeats).to.have.members(["A1", "E5", "E6"]);
    pm.expect(m.availableSeats).to.eql(37);
});`
    )
  ]
};

// Folder 07 - History & Cancel
const folder07 = {
  name: "07-History-Cancel",
  item: [
    req("7.1 Customer 1 xem lich su dat ve cua minh", "GET", "/api/bookings/my", "customerToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const list = pm.response.json();
pm.test("Only my bookings", () => list.forEach(b => pm.expect(b.customerId).to.eql(1)));
pm.test("Sorted by bookingDate DESC", () => {
    for (let i = 1; i < list.length; i++) {
        pm.expect(list[i - 1].bookingDate >= list[i].bookingDate).to.be.true;
    }
});`
    ),
    req("7.2 Customer 2 xem booking cua customer 1 - 403", "GET", "/api/bookings/{{bookingId}}", "customer2Token", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("7.3 Admin xem booking cua customer 1 - 200", "GET", "/api/bookings/{{bookingId}}", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));`
    ),
    req("7.4 Customer 1 huy booking cua customer 2 - 403", "PUT", "/api/bookings/{{booking2Id}}/cancel", "customerToken", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("7.5 Customer 2 huy booking cua chinh minh", "PUT", "/api/bookings/{{booking2Id}}/cancel", "customer2Token", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Cancelled", () => pm.expect(pm.response.json().bookingStatus).to.eql("CANCELLED"));`
    ),
    req("7.6 Customer 2 huy lai booking da huy - 400", "PUT", "/api/bookings/{{booking2Id}}/cancel", "customer2Token", null,
      errCheck(400)
    ),
    req("7.7 Customer 1 dat lai ghe A1 vua duoc giai phong", "POST", "/api/bookings", "customerToken",
      `{ "items": [{ "showtimeId": "{{showtimeId}}", "seatCode": "A1" }] }`,
      `pm.test("Status 201", () => pm.response.to.have.status(201));
pm.environment.set("booking3Id", pm.response.json().bookingId);`
    ),
    req("7.8 Admin xem tat ca booking", "GET", "/api/bookings", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("At least 3 bookings", () => pm.expect(pm.response.json().length).to.be.at.least(3));`
    ),
    req("7.9 Admin goi /api/bookings/my - 403", "GET", "/api/bookings/my", "adminToken", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("7.10 Xem booking khong ton tai - 404", "GET", "/api/bookings/99999", "adminToken", null,
      errCheck(404)
    )
  ]
};

// Folder 08 - Report
const folder08 = {
  name: "08-Report",
  item: [
    req("8.1 Admin tao bao cao doanh thu hom nay", "GET", "/api/bookings/report?startDate={{today}}&endDate={{today}}", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const r = pm.response.json();
pm.test("Summary is consistent", () => {
    pm.expect(r.totalBookings).to.eql(r.bookings.length);
    const tickets = r.bookings.reduce((a, b) => a + b.details.length, 0);
    const revenue = r.bookings.reduce((a, b) => a + Number(b.totalPrice), 0);
    pm.expect(r.totalTickets).to.eql(tickets);
    pm.expect(Number(r.totalRevenue)).to.eql(revenue);
});
pm.test("Only CONFIRMED bookings", () =>
    r.bookings.forEach(b => pm.expect(b.bookingStatus).to.eql("CONFIRMED")));
pm.test("Cancelled booking excluded", () => {
    const ids = r.bookings.map(b => b.bookingId);
    pm.expect(ids).to.not.include(Number(pm.environment.get("booking2Id")));
});
pm.test("bookings sorted by bookingDate DESC", () => {
    for (let i = 1; i < r.bookings.length; i++)
        pm.expect(r.bookings[i - 1].bookingDate >= r.bookings[i].bookingDate).to.be.true;
});
pm.test("revenueByMovie sorted by revenue DESC", () => {
    for (let i = 1; i < r.revenueByMovie.length; i++)
        pm.expect(Number(r.revenueByMovie[i - 1].revenue)).to.be.at.least(Number(r.revenueByMovie[i].revenue));
});`,
      `const pad = n => String(n).padStart(2, "0");
const d = new Date();
pm.environment.set("today", \`\${d.getFullYear()}-\${pad(d.getMonth() + 1)}-\${pad(d.getDate())}\`);`
    ),
    req("8.2 Bao cao - startDate lon hon endDate - 400", "GET", "/api/bookings/report?startDate=2026-12-31&endDate=2026-01-01", "adminToken", null,
      errCheck(400)
    ),
    req("8.3 Customer xem bao cao - 403", "GET", "/api/bookings/report?startDate={{today}}&endDate={{today}}", "customerToken", null,
      `pm.test("Status 403", () => pm.response.to.have.status(403));`
    ),
    req("8.4 Bao cao thieu tham so endDate - 400", "GET", "/api/bookings/report?startDate={{today}}", "adminToken", null,
      errCheck(400)
    ),
    req("8.5 Bao cao ky khong co giao dich - 200 rong", "GET", "/api/bookings/report?startDate=2020-01-01&endDate=2020-01-31", "adminToken", null,
      `pm.test("Status 200", () => pm.response.to.have.status(200));
const r = pm.response.json();
pm.test("Zero bookings and revenue", () => {
    pm.expect(r.totalBookings).to.eql(0);
    pm.expect(Number(r.totalRevenue)).to.eql(0);
});`
    )
  ]
};

const collection = {
  info: {
    _postman_id: "788a1b2c-3d4e-5f60-7a8b-9c0d1e2f3a4b",
    name: "FUCinemaBookingSystem",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  item: [
    folder01,
    folder02,
    folder03,
    folder04,
    folder05,
    folder06,
    folder07,
    folder08
  ]
};

const outDir = path.join(__dirname, '..', 'postman');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

fs.writeFileSync(path.join(outDir, 'FUCinema-Local.postman_environment.json'), JSON.stringify(env, null, 2));
fs.writeFileSync(path.join(outDir, 'FUCinemaBookingSystem.postman_collection.json'), JSON.stringify(collection, null, 2));
console.log('Postman collection & environment written to postman/ successfully!');
